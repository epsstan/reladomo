/*
  Copyright 2016 Goldman Sachs.
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
 */

package com.gs.fw.common.mithra.util.serializer;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.fw.common.mithra.*;
import com.gs.fw.common.mithra.attribute.*;
import com.gs.fw.common.mithra.behavior.state.DatedDeletedState;
import com.gs.fw.common.mithra.behavior.state.PersistedState;
import com.gs.fw.common.mithra.cache.FullUniqueIndex;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.NoOperation;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.*;
import com.gs.fw.common.mithra.cache.HashStrategy;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is not thread safe. A deserializer must be instantiated for every deserialized stream.
 *
 * "Field" means either an attribute or an annotated method.
 *
 * @param <T>
 */
public class ReladomoDeserializer<T extends MithraObject>
{
    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final Object[] NULL_ARGS = (Object[]) null;
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat(DATE_FORMAT);

    //todo: key here should be both class and finder to support inheritance
    private static final Function<RelatedFinder, DeserializationClassMetaData> DESERIALIZATION_META_DATA_FACTORY = new Function<RelatedFinder, DeserializationClassMetaData>()
    {
        @Override
        public DeserializationClassMetaData valueOf(RelatedFinder relatedFinder)
        {
            return new DeserializationClassMetaData(relatedFinder);
        }
    };

    public enum FieldOrRelation
    {
        Attribute, ToOneRelationship, ToManyRelationship, AnnotatedMethod, Unknown
    }

    private static final UnifiedMap<String, DeserializationClassMetaData> DESERIALIZATION_META_DATA_CACHE = UnifiedMap.newMap();

    private StackableData data;
    private Stack<StackableData> dataStack = new Stack<StackableData>();
    private Map<DeserializationClassMetaData, List<PartialDeserialized>> objectsToResolve = UnifiedMap.newMap();

    public ReladomoDeserializer(RelatedFinder<T> rootFinder)
    {
        this.data = new StackableData();
        this.data.metaData = findDeserializationMetaData(rootFinder);
        this.data.currentState = StartStateHaveMeta.INSTANCE;
    }

    public ReladomoDeserializer()
    {
        this.data = new StackableData();
        this.data.currentState = StartStateNoMeta.INSTANCE;
    }

    protected DeserializationClassMetaData findDeserializationMetaData(RelatedFinder<T> finder)
    {
        synchronized (DESERIALIZATION_META_DATA_CACHE)
        {
            return DESERIALIZATION_META_DATA_CACHE.getIfAbsentPutWith(finder.getFinderClassName(), DESERIALIZATION_META_DATA_FACTORY, finder);
        }
    }

    protected void pushDataAndStartEmpty(RelatedFinder finder)
    {
        pushDataAndStartEmpty(findDeserializationMetaData(finder));
    }

    protected void pushDataAndStartEmpty(DeserializationClassMetaData deserializationClassMetaData)
    {
        dataStack.push(data);
        this.data = new StackableData();
        this.data.metaData = deserializationClassMetaData;
    }

    protected boolean popData()
    {
        if (dataStack.size() > 0)
        {
            this.data = dataStack.pop();
            return true;
        }
        return false;
    }

    public void storeReladomoClassName(String className) throws DeserializationException
    {
        this.data.currentState.storeReladomoClassName(this, className);
    }

    public void startObject() throws DeserializationException
    {
        this.data.currentState.startObject(this);
    }

    public void endObject() throws DeserializationException
    {
        this.data.currentState.endObject(this);
    }

    public void startList() throws DeserializationException
    {
        this.data.currentState.startList(this);
    }

    public void endList() throws DeserializationException
    {
        this.data.currentState.endList(this);
    }

    public FieldOrRelation startFieldOrRelationship(String name) throws DeserializationException
    {
        return this.data.currentState.startFieldOrRelationship(name, this);
    }

    /**
     * skips this field or relationship, as if it was not in the stream. This 
     * is NOT equivalent to being null.
     * 
     * @throws DeserializationException
     */
    public void skipCurrentFieldOrRelationship() throws DeserializationException
    {
        this.data.currentState.skipCurrentFieldOrRelationship(this);
    }

    /**
     *
     * @param name
     * @return null if the name is not an attribute. Must call skipAttributeOrRelationship if the return value is null.
     * @throws DeserializationException
     */
    public Attribute startAttribute(String name) throws DeserializationException
    {
        return this.data.currentState.startAttribute(name, this);
    }

    /**
     *
     * @param name
     * @return ToOneRelationship, ToManyRelationship, Unknown. Must call skipAttributeOrRelationship if the return value is Unknown
     */
    public FieldOrRelation startRelationship(String name) throws DeserializationException
    {
        return this.data.currentState.startRelationship(name, this);
    }

    /**
     * works for attributes and annotated methods. The following formats are the only ones expected:
     * Date: yyyy-MM-dd
     * Timestamp: yyyy-MM-dd HH:mm:ss.SSS
     * Time: HH:mm:ss.SSS
     * byte array: hex encoded values, 2 chars per byte, no 0x in front, e.g. DEA307 is a 3 byte array.
     * boolean: true/false or 1/0
     * numbers: anything parsable by the parse method on the boxed wrapper.
     * 
     * the value "null" is handled for all types, except for String attributes. Call setFieldOrRelationshipNull for strings
     * or anywhere you need to signal null explicitly.
     * 
     * If you need a different format, parse it yourself and call the appropriate set methods (e.g. setTimestampField)
     * @param value
     * @throws DeserializationException
     */
    public void parseFieldFromString(String value) throws DeserializationException
    {
        this.data.currentState.parseFieldFromString(value, this);
    }
    
    public void setByteField(byte value) throws DeserializationException
    {
        this.data.currentState.setByteField(value, this);
    }
    
    public void setShortField(short value) throws DeserializationException
    {
        this.data.currentState.setShortField(value, this);
    }

    public void setIntField(int value) throws DeserializationException
    {
        this.data.currentState.setIntField(value, this);
    }

    public void setLongField(long value) throws DeserializationException
    {
        this.data.currentState.setLongField(value, this);
    }
    
    public void setFloatField(float value) throws DeserializationException
    {
        this.data.currentState.setFloatField(value, this);
    }
    
    public void setDoubleField(double value) throws DeserializationException
    {
        this.data.currentState.setDoubleField(value, this);
    }
    
    public void setBooleanField(boolean value) throws DeserializationException
    {
        this.data.currentState.setBooleanField(value, this);
    }

    public void setCharField(char value) throws DeserializationException
    {
        this.data.currentState.setCharField(value, this);
    }

    public void setByteArrayField(byte[] value) throws DeserializationException
    {
        this.data.currentState.setByteArrayField(value, this);
    }

    public void setStringField(String value) throws DeserializationException
    {
        this.data.currentState.setStringField(value, this);
    }
    
    public void setBigDecimalField(BigDecimal value) throws DeserializationException
    {
        this.data.currentState.setBigDecimalField(value, this);
    }
    
    public void setDateField(Date value) throws DeserializationException
    {
        this.data.currentState.setDateField(value, this);
    }
    
    public void setTimestampField(Timestamp value) throws DeserializationException
    {
        this.data.currentState.setTimestampField(value, this);
    }
    
    public void setTimeField(Time value) throws DeserializationException
    {
        this.data.currentState.setTimeField(value, this);
    }
    
    public void setFieldOrRelationshipNull() throws DeserializationException
    {
        this.data.currentState.setFieldOrRelationshipNull(this);
    }

    public void startListElements() throws DeserializationException
    {
        this.data.currentState.startListElements(this);
    }

    public void endListElements() throws DeserializationException
    {
        this.data.currentState.endListElements(this);
    }

    /**
     * valid values are:
     *
     * ReladomoSerializationContext.READ_ONLY_STATE
     * ReladomoSerializationContext.DETACHED_STATE
     * ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE
     * ReladomoSerializationContext.IN_MEMORY_STATE
     *
     * In the final stage of deserialization, the database is queried to determine how the object should
     * behave after deserialization.
     *
     * If this method is not called, the object will be automatically deserialized in the following states:
     * - detached: if the pk specified exists in the database and the object is transactional.
     * - read-only: if the pk specified exists in the database and the object is read-only.
     * - in-memory: if the pk specified does not exist in the database.
     *
     * If the method is called, then following behavior is enforced:
     *
     * ReladomoSerializationContext.READ_ONLY_STATE: object must be read-only and must exist in the database
     * ReladomoSerializationContext.DETACHED_STATE: object must be transactional and must exist in the database
     * ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE: object must be transactional and must exist in the database
     * ReladomoSerializationContext.IN_MEMORY_STATE: nothing is enforced. If this object is later inserted, application must ensure no duplicate exists.
     *
     * @param objectState
     * @throws DeserializationException
     */
    public void setReladomoObjectState(int objectState) throws DeserializationException
    {
        this.data.currentState.setReladomoObjectState(objectState, this);
    }

    private void addSingleObjectToResolve() throws DeserializationException
    {
        AsOfAttribute businessDateAttribute = this.data.metaData.getBusinessDateAttribute();
        if (businessDateAttribute != null && this.data.partial.businessDate == null && businessDateAttribute.getDefaultDate() == null)
        {
            throw new DeserializationException("Business date must be supplied for "+this.data.partial.dataObject.zGetPrintablePrimaryKey());
        }
        DeserializationClassMetaData key = this.data.metaData;
        List<PartialDeserialized> partialDeserializeds = objectsToResolve.get(key);
        if (partialDeserializeds == null)
        {
            partialDeserializeds = FastList.newList();
            objectsToResolve.put(this.data.metaData, partialDeserializeds);
        }
        partialDeserializeds.add(this.data.partial);
    }

    public Serialized<T> getDeserializedResult() throws DeserializationException
    {
        return new Serialized<T>(this);
    }

    public T getDeserializationResultAsObject()
    {
        try
        {
            if (this.data.metaData.isConfigured())
            {
                return findAndDeserializeSingleObject();
            }
            else
            {
                return desrialzieSingleObjectAsInMemory();
            }
        }
        catch (DeserializationException e)
        {
            throw new RuntimeException("Could not deserialize", e);
        }
    }

    private T desrialzieSingleObjectAsInMemory()
    {
        //todo
        throw new RuntimeException("not yet");
    }

    protected void checkSingleObjectDeserialized() throws DeserializationException
    {
        if (this.data.partial == null)
        {
            String message = "No object was deserialized!";
            if (this.data.list != null)
            {
                message = "Looks like we deserialized a list, not an object!";
            }
            throw new DeserializationException(message);
        }
    }

    protected T findAndDeserializeSingleObject() throws DeserializationException
    {
        resolveAndFetchAllObjects();
        return (T) this.data.partial.deserialized;
    }

    protected void resolveAndFetchAllObjects() throws DeserializationException
    {
        for (Map.Entry<DeserializationClassMetaData, List<PartialDeserialized>> entry : objectsToResolve.entrySet())
        {
            resolveList(entry.getKey(), entry.getValue());
        }
        wireRelationships();
    }

    private void wireRelationships()
    {
        //todo
        throw new RuntimeException("not yet");
    }

    private List<List<PartialDeserialized>> segregateByAsOfAndSourceAttribute(DeserializationClassMetaData metaData, List<PartialDeserialized> dbLookUp)
    {
        if (dbLookUp.size() == 1)
        {
            return ListFactory.create(dbLookUp);
        }
        PartialDeserializedSegregationHashingStrategy hashStrategy = new PartialDeserializedSegregationHashingStrategy(metaData.getSourceAttribute(),
                metaData.getProcessingDateAttribute(), metaData.getBusinessDateAttribute());
        //todo: optimize this for non-repeated asOfAttributes
        MultiHashMap map = new MultiHashMap();
        for (int i = 0; i < dbLookUp.size(); i++)
        {
            Object o = dbLookUp.get(i);
            map.put(new KeyWithHashStrategy(o, hashStrategy), o);
        }

        if (map.size() > 1)
        {
            return map.valuesAsList();
        }
        else
        {
            return ListFactory.create(dbLookUp);
        }
    }

    protected void resolveList(DeserializationClassMetaData metaData, List<PartialDeserialized> listToResolve) throws DeserializationException
    {
        List<PartialDeserialized> dbLookUp = FastList.newList(listToResolve.size());
        for(PartialDeserialized partDes: listToResolve)
        {
            switch (partDes.state)
            {
                case 0: // not set
                case ReladomoSerializationContext.READ_ONLY_STATE:
                case ReladomoSerializationContext.DETACHED_STATE:
                case ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE:
                    dbLookUp.add(partDes);
                    break;
                case ReladomoSerializationContext.IN_MEMORY_STATE:
                    handleInMemoryObject(partDes, metaData);
                    break;
            }
        }

        if (!dbLookUp.isEmpty())
        {
            RelatedFinder finder = metaData.getRelatedFinder();
            Attribute[] pkAttributesNoSource = metaData.getPrimaryKeyAttributesWithoutSource();
            Attribute sourceAttribute = metaData.getSourceAttribute();
            AsOfAttribute[] asOfAttributes = finder.getAsOfAttributes();
            if (asOfAttributes != null || sourceAttribute != null)
            {
                List<List<PartialDeserialized>> segregated = segregateByAsOfAndSourceAttribute(metaData, dbLookUp);
                for (int i = 0; i < segregated.size(); i++)
                {
                    List<PartialDeserialized> list = segregated.get(i);
                    Operation segOp = NoOperation.instance();
                    PartialDeserialized first = list.get(0);
                    if (sourceAttribute != null)
                    {
                        if (!first.isAttributeSet(sourceAttribute, metaData))
                        {
                            handleUnresolvableObjects(list, metaData);
                            break; // can't be resolved.
                        }
                        segOp = segOp.and(sourceAttribute.nonPrimitiveEq(sourceAttribute.valueOf(first.dataObject)));
                    }
                    if (metaData.getBusinessDateAttribute() != null && first.businessDate != null)
                    {
                        segOp = segOp.and(metaData.getBusinessDateAttribute().eq(first.businessDate));
                    }
                    if (metaData.getProcessingDateAttribute() != null && first.processingDate != null)
                    {
                        segOp = segOp.and(metaData.getProcessingDateAttribute().eq(first.processingDate));
                    }
                    forceRefresh(metaData, list, pkAttributesNoSource, segOp);
                }
            }
            else
            {
                forceRefresh(metaData, dbLookUp, pkAttributesNoSource, null);
            }
        }
    }

    private void handleUnresolvableObjects(List<PartialDeserialized> segregated, DeserializationClassMetaData metaData) throws DeserializationException
    {
        for(PartialDeserialized partDes: segregated)
        {
            switch (partDes.state)
            {
                case 0: // not set
                    handleInMemoryObject(partDes, metaData);
                    break;
                case ReladomoSerializationContext.READ_ONLY_STATE:
                case ReladomoSerializationContext.DETACHED_STATE:
                case ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE:
                    throw new DeserializationException("SourceAttribute missing in serial stream for "+partDes.dataObject.zGetPrintablePrimaryKey());
            }
        }

    }

    private void handleInMemoryObject(PartialDeserialized partDes, DeserializationClassMetaData metaData) throws DeserializationException
    {
        MithraObject inMemory = constructObjectAndSetAttributes(partDes, metaData);
        partDes.deserialized = inMemory;
    }

    private void forceRefresh(DeserializationClassMetaData metaData, List<PartialDeserialized> listToRefresh, Attribute[] pkAttributes, Operation extraOp) throws DeserializationException
    {
        List<MithraDataObject> unwrapped = FastList.newList(listToRefresh.size());
        for (int i = 0; i < listToRefresh.size(); i++)
        {
            PartialDeserialized partial = listToRefresh.get(i);
            if (arePkAttributesSetWithNullableCheck(metaData, pkAttributes, partial))
            {
                unwrapped.add(partial.dataObject);
            }
        }
        Operation op = extraOp;
        if (pkAttributes.length == 1)
        {
            op = op.and(constructSinglePkOp(unwrapped, pkAttributes[0]));
        }
        else
        {
            op = op.and(constructMultiPkOp(unwrapped, pkAttributes));
        }
        MithraList many = metaData.getRelatedFinder().findMany(op);
        FullUniqueIndex index = new FullUniqueIndex(pkAttributes, many.size());
        index.addAll(many);

        for (int i = 0; i < listToRefresh.size(); i++)
        {
            PartialDeserialized partial = listToRefresh.get(i);
            if (arePkAttributesSet(metaData, pkAttributes, partial))
            {
                MithraObject fromDb = (MithraObject) index.get(partial.dataObject);
                if (fromDb != null)
                {
                    handleFoundObject(fromDb, partial, metaData);
                }
                else
                {
                    handleNotFoundObject(partial, metaData);
                }
            }
        }
    }

    private void handleNotFoundObject(PartialDeserialized partial, DeserializationClassMetaData metaData) throws DeserializationException
    {
        MithraObject inMemory;
        switch (partial.state)
        {
            case 0: // not set
                inMemory = constructObjectAndSetAttributes(partial, metaData);
                partial.deserialized = inMemory;
                break;
            case ReladomoSerializationContext.READ_ONLY_STATE:
            case ReladomoSerializationContext.DETACHED_STATE:
                throw new DeserializationException("Object "+partial.dataObject.zGetPrintablePrimaryKey()+" doesn't exist in the db");
            case ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE:
                inMemory = constructObjectAndSetAttributes(partial, metaData);
                inMemory.zSetNonTxPersistenceState(PersistedState.DELETED);
                partial.deserialized = inMemory;
                break;
            default:
                throw new DeserializationException("should not get here!");
        }
    }

    private MithraObject constructObjectAndSetAttributes(PartialDeserialized partial, DeserializationClassMetaData metaData) throws DeserializationException
    {
        MithraObject obj = metaData.constructObject(partial.businessDate, partial.processingDate);
        setAttributesAndMethods(obj, partial, metaData);
        return obj;
    }

    private void setAttributesAndMethods(MithraObject obj, PartialDeserialized partial, DeserializationClassMetaData metaData)
    {
        //todo
        throw new RuntimeException("not yet");
    }

    private void handleFoundObject(MithraObject fromDb, PartialDeserialized partial, DeserializationClassMetaData metaData) throws DeserializationException
    {
        MithraTransactionalObject detachedCopy;
        switch (partial.state)
        {
            case 0: // not set
                if (metaData.getRelatedFinder().getMithraObjectPortal().isTransactional())
                {
                    detachedCopy = ((MithraTransactionalObject) fromDb).getDetachedCopy();
                    setAttributesAndMethods(detachedCopy, partial, metaData);
                    partial.deserialized = detachedCopy;
                }
                else
                {
                    partial.deserialized = fromDb;
                }
                break;
            case ReladomoSerializationContext.READ_ONLY_STATE:
                partial.deserialized = fromDb;
                break;
            case ReladomoSerializationContext.DETACHED_STATE:
                detachedCopy = ((MithraTransactionalObject) fromDb).getDetachedCopy();
                setAttributesAndMethods(detachedCopy, partial, metaData);
                partial.deserialized = detachedCopy;
                break;
            case ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE:
                detachedCopy = ((MithraTransactionalObject) fromDb).getDetachedCopy();
                if (metaData.getRelatedFinder().getAsOfAttributes() != null)
                {
                    ((MithraDatedTransactionalObject)detachedCopy).terminate();
                }
                else
                {
                    detachedCopy.delete();
                }
                partial.deserialized = detachedCopy;
                break;
            default:
                throw new DeserializationException("should not get here!");
        }
    }

    private boolean arePkAttributesSetWithNullableCheck(DeserializationClassMetaData metaData, Attribute[] pkAttributes, PartialDeserialized partial)
    {
        for (Attribute a : pkAttributes)
        {
            if (!partial.isAttributeSet(a, metaData))
            {
                if (a.getMetaData().isNullable())
                {
                    a.setValueNull(partial.dataObject);
                    partial.markAttributeDone(a, metaData);
                }
                else
                {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean arePkAttributesSet(DeserializationClassMetaData metaData, Attribute[] pkAttributes, PartialDeserialized partial)
    {
        for (Attribute a : pkAttributes)
        {
            if (!partial.isAttributeSet(a, metaData))
            {
                return false;
            }
        }
        return true;
    }

    private Operation constructMultiPkOp(List<MithraDataObject> listToRefresh, Attribute[] pkAttributes)
    {
        TupleAttribute ta = pkAttributes[0].tupleWith(pkAttributes[1]);
        for(int i=2;i<pkAttributes.length;i++)
        {
            ta = ta.tupleWith(pkAttributes[i]);
        }
        return ta.in(listToRefresh, pkAttributes);
    }

    private Operation constructSinglePkOp(List<MithraDataObject> listToRefresh, Attribute pkAttribute)
    {
        return pkAttribute.in(listToRefresh, pkAttribute);
    }


    //    public MithraList<T> getDeserializationResultAsList()
//    {
//        resolveStuff();
//        //todo
//    }

    protected Object invokeStaticMethod(Class classToInvoke, String methodName) throws DeserializationException
    {
        try
        {
            Method method = ReflectionMethodCache.getZeroArgMethod(classToInvoke, methodName);
            return method.invoke(null, NULL_ARGS);
        }
        catch (Exception e)
        {
            throw new DeserializationException("Could not invoke method "+methodName+" on class "+classToInvoke, e);
        }
    }

    protected static class StackableData
    {
        protected DeserializationClassMetaData metaData;
        protected PartialDeserialized partial;
        protected List<PartialDeserialized> list;
        protected Attribute attribute;
        protected RelatedFinder related;
        protected Method method;
        protected State currentState;
    }

    protected static class PartialDeserialized
    {
        protected MithraDataObject dataObject;
        protected Timestamp businessDate;
        protected Timestamp processingDate;
        protected BitSet populatedAttributes;
        protected int state;
        protected Map<String, Object> partialRelationships; //Object is either PartialDeserialized or List<PartialDeserialized>
        protected MithraObject deserialized; // constructed at the end of the stream

        protected void storeRelated(RelatedFinder relatedFinder, Object related)
        {
            if (this.partialRelationships == null)
            {
                this.partialRelationships = UnifiedMap.newMap();
            }
            this.partialRelationships.put(((AbstractRelatedFinder)relatedFinder).getRelationshipName(), related);
        }


        public void markAttributeDone(Attribute attribute, DeserializationClassMetaData metaData)
        {
            if (populatedAttributes == null)
            {
                populatedAttributes = new BitSet(metaData.getTotalAttributes());
            }
            populatedAttributes.set(metaData.getAttriutePosition(attribute));
        }

        public boolean isAttributeSet(Attribute attr, DeserializationClassMetaData metaData)
        {
            return populatedAttributes != null && populatedAttributes.get(metaData.getAttriutePosition(attr));
        }
    }

    protected static class PartialDeserializedSegregationHashingStrategy implements HashStrategy
    {
        private Attribute sourceAttribute;
        private AsOfAttribute processingDateAttr;
        private AsOfAttribute businessDateAttr;

        public PartialDeserializedSegregationHashingStrategy(Attribute sourceAttribute, AsOfAttribute processingDateAttr, AsOfAttribute businessDateAttr)
        {
            this.sourceAttribute = sourceAttribute;
            this.processingDateAttr = processingDateAttr;
            this.businessDateAttr = businessDateAttr;
        }

        @Override
        public int computeHashCode(Object o)
        {
            PartialDeserialized partialDeserialized = (PartialDeserialized) o;
            int hash = HashUtil.NULL_HASH;
            if (sourceAttribute != null)
            {
                hash = HashUtil.combineHashes(hash, sourceAttribute.valueHashCode(partialDeserialized.dataObject));
            }
            if (businessDateAttr != null)
            {
                hash = HashUtil.combineHashes(hash, partialDeserialized.businessDate.hashCode());
            }
            if (processingDateAttr != null && partialDeserialized.processingDate != null)
            {
                hash = HashUtil.combineHashes(hash, partialDeserialized.processingDate.hashCode());
            }
            return hash;
        }

        @Override
        public boolean equals(Object o1, Object o2)
        {
            PartialDeserialized left = (PartialDeserialized) o1;
            PartialDeserialized right = (PartialDeserialized) o2;
            if (sourceAttribute != null && !sourceAttribute.valueEquals(left.dataObject, right.dataObject))
            {
                return false;
            }
            if (businessDateAttr != null && !left.businessDate.equals(right.businessDate))
            {
                return false;
            }
            if (processingDateAttr != null)
            {
                if (left.processingDate == right.processingDate) // covers both null
                {
                    return true;
                }
                if (left.processingDate != null)
                {
                    return left.processingDate.equals(right.processingDate) || (left.processingDate.equals(processingDateAttr.getDefaultDate()) && right.processingDate == null);
                }
                return right.processingDate.equals(left.processingDate) || (right.processingDate.equals(processingDateAttr.getDefaultDate()) && left.processingDate == null);
            }
            return true;
        }
    }

    protected static class StartStateNoMeta extends State
    {
        private static final StartStateNoMeta INSTANCE = new StartStateNoMeta();

        @Override
        public void storeReladomoClassName(ReladomoDeserializer deserializer, String className) throws DeserializationException
        {
            RelatedFinder relatedFinder = getFinderInstance(deserializer, className);
            deserializer.data.metaData = deserializer.findDeserializationMetaData(relatedFinder);
            deserializer.data.currentState = StartStateHaveMeta.INSTANCE;
        }
    }

    protected static RelatedFinder getFinderInstance(ReladomoDeserializer deserializer, String className) throws DeserializationException
    {
        //todo: polymorphic case; need to search up until we find a finder. Also store the classname
        String finderClassName = className + "Finder";
        try
        {
            Class<?> finderClass = Class.forName(finderClassName);
            return (RelatedFinder) deserializer.invokeStaticMethod(finderClass, "getFinderInstance");
        }
        catch (ClassNotFoundException e)
        {
            throw new DeserializationException("Could not find class "+finderClassName, e);
        }
    }

    protected static void checkClassNameConsistency(ReladomoDeserializer deserializer, String className) throws DeserializationException
    {
        RelatedFinder finderInstance = getFinderInstance(deserializer, className);
        if (!deserializer.data.metaData.getRelatedFinder().getFinderClassName().equals(finderInstance.getFinderClassName()))
        {
            throw new DeserializationException("inconsistent meta data. was expecting "+deserializer.data.metaData.getRelatedFinder().getFinderClassName()+"" +
                    " but got "+finderInstance.getFinderClassName());
        }
    }

    protected static class StartStateHaveMeta extends State
    {
        private static final StartStateHaveMeta INSTANCE = new StartStateHaveMeta();

        @Override
        public void storeReladomoClassName(ReladomoDeserializer deserializer, String className) throws DeserializationException
        {
            checkClassNameConsistency(deserializer, className);
        }

        @Override
        public void startObject(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.pushDataAndStartEmpty(deserializer.data.metaData);
            commonStartState(deserializer);
        }

        @Override
        public void storeNested(ReladomoDeserializer deserializer, PartialDeserialized toSave) throws DeserializationException
        {
            deserializer.data.partial = toSave;
            deserializer.data.currentState = EndState.INSTANCE;
        }

        @Override
        public void storeNestedList(ReladomoDeserializer deserializer, List<PartialDeserialized> toSave) throws DeserializationException
        {
            deserializer.data.list = toSave;
            deserializer.data.currentState = EndState.INSTANCE;
        }

        @Override
        public void startList(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.pushDataAndStartEmpty(deserializer.data.metaData);
            deserializer.data.currentState = InListState.INSTANCE;
        }
    }

    protected static class EndState extends State
    {
        private static final EndState INSTANCE = new EndState();
    }

    protected static class InObjectState extends State
    {
        private static final InObjectState INSTANCE = new InObjectState();

        @Override
        public void storeReladomoClassName(ReladomoDeserializer deserializer, String className) throws DeserializationException
        {
            checkClassNameConsistency(deserializer, className);
        }

        @Override
        public void endObject(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.addSingleObjectToResolve();
            StackableData toSave = deserializer.data;
            if (deserializer.popData())
            {
                deserializer.data.currentState.storeNested(deserializer, toSave.partial);
            }
        }

        @Override
        public void setReladomoObjectState(int objectState, ReladomoDeserializer deserializer) throws DeserializationException
        {
            if (objectState != ReladomoSerializationContext.DETACHED_STATE &&
                    objectState != ReladomoSerializationContext.IN_MEMORY_STATE &&
                    objectState != ReladomoSerializationContext.READ_ONLY_STATE &&
                    objectState != ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE)
            {
                throw new DeserializationException("Unrecognized object state "+objectState);
            }
            deserializer.data.partial.state = objectState;
        }

        @Override
        public FieldOrRelation startFieldOrRelationship(String name, ReladomoDeserializer deserializer) throws DeserializationException
        {
            Attribute attribute = deserializer.data.metaData.getAttributeByName(name);
            if (attribute != null)
            {
                deserializer.data.attribute = attribute;
                deserializer.data.currentState = InAttributeState.INSTANCE;
                return FieldOrRelation.Attribute;
            }
            RelatedFinder relatedFinder = deserializer.data.metaData.getRelationshipFinderByName(name);
            if (relatedFinder != null)
            {
                deserializer.data.related = relatedFinder;
                if (((AbstractRelatedFinder)relatedFinder).isToOne())
                {
                    deserializer.data.currentState = InToOneRelationshipState.INSTANCE;
                    return FieldOrRelation.ToOneRelationship;
                }
                else
                {
                    deserializer.data.currentState = InToManyRelationshipState.INSTANCE;
                    return FieldOrRelation.ToManyRelationship;
                }
            }
            Method annotatedMethod = deserializer.data.metaData.getAnnotatedMethodByName(name);
            if (annotatedMethod != null)
            {
                deserializer.data.method = annotatedMethod;
                deserializer.data.currentState = InAnnotatedMethod.INSTANCE;
                return FieldOrRelation.AnnotatedMethod;
            }
            deserializer.data.currentState = InUnknownField.INSTANCE;
            return FieldOrRelation.Unknown;
        }

        @Override
        public Attribute startAttribute(String name, ReladomoDeserializer deserializer) throws DeserializationException
        {
            Attribute attribute = deserializer.data.metaData.getAttributeByName(name);
            if (attribute != null)
            {
                deserializer.data.attribute = attribute;
                deserializer.data.currentState = InAttributeState.INSTANCE;
            }
            else
            {
                deserializer.data.currentState = InUnknownField.INSTANCE;
            }
            return attribute;
        }

        @Override
        public FieldOrRelation startRelationship(String name, ReladomoDeserializer deserializer) throws DeserializationException
        {
            RelatedFinder relatedFinder = deserializer.data.metaData.getRelationshipFinderByName(name);
            if (relatedFinder != null)
            {
                deserializer.data.related = relatedFinder;
                if (((AbstractRelatedFinder)relatedFinder).isToOne())
                {
                    deserializer.data.currentState = InToOneRelationshipState.INSTANCE;
                    return FieldOrRelation.ToOneRelationship;
                }
                else
                {
                    deserializer.data.currentState = InToManyRelationshipState.INSTANCE;
                    return FieldOrRelation.ToManyRelationship;
                }
            }
            deserializer.data.currentState = InUnknownField.INSTANCE;
            return FieldOrRelation.Unknown;
        }
    }

    protected static class InUnknownField extends State
    {
        private static final InUnknownField INSTANCE = new InUnknownField();

        @Override
        public void skipCurrentFieldOrRelationship(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.data.currentState = InObjectState.INSTANCE;
        }
    }

    protected static class InToManyRelationshipState extends State
    {
        private static final InToManyRelationshipState INSTANCE = new InToManyRelationshipState();

        @Override
        public void startList(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.pushDataAndStartEmpty(deserializer.data.metaData);
            deserializer.data.currentState = InListState.INSTANCE;
        }

        @Override
        public void storeNestedList(ReladomoDeserializer deserializer, List<PartialDeserialized> toSave) throws DeserializationException
        {
            deserializer.data.partial.storeRelated(deserializer.data.related, toSave);
            deserializer.data.currentState = InObjectState.INSTANCE;
        }
    }

    protected static class InListState extends State
    {
        private static final InListState INSTANCE = new InListState();

        @Override
        public void startListElements(ReladomoDeserializer deserializer) throws DeserializationException
        {
            if (deserializer.data.list != null)
            {
                throw new DeserializationException("Cannot startListElements twice on the same list!");
            }
            deserializer.pushDataAndStartEmpty(deserializer.data.metaData);
            deserializer.data.currentState = InListElementState.INSTANCE;
        }

        @Override
        public void storeNestedList(ReladomoDeserializer deserializer, List<PartialDeserialized> toSave) throws DeserializationException
        {
            deserializer.data.list = toSave;
        }

        @Override
        public void endList(ReladomoDeserializer deserializer) throws DeserializationException
        {
            List<PartialDeserialized> toSave = deserializer.data.list;
            deserializer.popData();
            deserializer.data.currentState.storeNestedList(deserializer, toSave);
        }
    }

    protected static class InListElementState extends State
    {
        private static final InListElementState INSTANCE = new InListElementState();

        @Override
        public void startObject(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.pushDataAndStartEmpty(deserializer.data.metaData);
            commonStartState(deserializer);
        }

        @Override
        public void storeNested(ReladomoDeserializer deserializer, PartialDeserialized toSave) throws DeserializationException
        {
            deserializer.data.list.add(toSave);
        }

        @Override
        public void endListElements(ReladomoDeserializer deserializer) throws DeserializationException
        {
            List<PartialDeserialized> toSave = deserializer.data.list;
            deserializer.popData();
            deserializer.data.currentState.storeNestedList(deserializer, toSave);
        }
    }

    protected static class InToOneRelationshipState extends State
    {
        private static final InToOneRelationshipState INSTANCE = new InToOneRelationshipState();

        @Override
        public void startObject(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.pushDataAndStartEmpty(deserializer.data.related);
            commonStartState(deserializer);
        }

        @Override
        public void storeNested(ReladomoDeserializer deserializer, PartialDeserialized toSave) throws DeserializationException
        {
            deserializer.data.partial.storeRelated(deserializer.data.related, toSave);
            deserializer.data.currentState = InObjectState.INSTANCE;
        }

        @Override
        public void setFieldOrRelationshipNull(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.data.partial.storeRelated(deserializer.data.related, null);
            deserializer.data.currentState = InObjectState.INSTANCE;
        }
    }

    protected static class InAnnotatedMethod extends State
    {
        private static final InAnnotatedMethod INSTANCE = new InAnnotatedMethod();

        //todo
    }

    protected static class InAttributeState extends State
    {
        private static final InAttributeState INSTANCE = new InAttributeState();

        private void attributeDone(ReladomoDeserializer deserializer)
        {
            deserializer.data.partial.markAttributeDone(deserializer.data.attribute, deserializer.data.metaData);
            deserializer.data.currentState = InObjectState.INSTANCE;
        }

        @Override
        public void parseFieldFromString(String value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            if (deserializer.data.attribute instanceof AsOfAttribute)
            {
                AsOfAttribute asOfAttribute = (AsOfAttribute) deserializer.data.attribute;
                Timestamp parsed = null;
                try
                {
                    parsed = new ImmutableTimestamp(deserializer.timestampFormat.parse(value).getTime());
                }
                catch (ParseException e)
                {
                    throw new DeserializationException("Could not parse " + value, e);
                }
                if (asOfAttribute.isProcessingDate())
                {
                    deserializer.data.partial.processingDate = parsed;
                }
                else
                {
                    deserializer.data.partial.businessDate = parsed;
                }
            }
            else
            {
                if (("null".equals(value) || value == null) && !(deserializer.data.attribute instanceof StringAttribute))
                {
                    deserializer.data.attribute.setValueNull(deserializer.data.partial.dataObject);
                }
                else
                {
                    try
                    {
                        deserializer.data.attribute.parseStringAndSet(value, deserializer.data.partial.dataObject, 0, deserializer.timestampFormat);
                    }
                    catch (ParseException e)
                    {
                        throw new DeserializationException("Could not parse " + value, e);
                    }
                }
            }
            attributeDone(deserializer);
        }

        @Override
        public void setFieldOrRelationshipNull(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.data.attribute.setValueNull(deserializer.data.partial);
            attributeDone(deserializer);
        }

        @Override
        public void setByteField(byte value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((ByteAttribute)deserializer.data.attribute).setByteValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setShortField(short value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((ShortAttribute)deserializer.data.attribute).setShortValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setIntField(int value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((IntegerAttribute)deserializer.data.attribute).setIntValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setLongField(long value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((LongAttribute)deserializer.data.attribute).setLongValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setFloatField(float value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((FloatAttribute)deserializer.data.attribute).setFloatValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setDoubleField(double value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((DoubleAttribute)deserializer.data.attribute).setDoubleValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setBooleanField(boolean value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((BooleanAttribute)deserializer.data.attribute).setBooleanValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setCharField(char value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((CharAttribute)deserializer.data.attribute).setCharValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setByteArrayField(byte[] value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((ByteArrayAttribute)deserializer.data.attribute).setByteArrayValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setStringField(String value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((StringAttribute)deserializer.data.attribute).setStringValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setBigDecimalField(BigDecimal value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((BigDecimalAttribute)deserializer.data.attribute).setBigDecimalValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setDateField(Date value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((DateAttribute)deserializer.data.attribute).setDateValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void setTimestampField(Timestamp value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            if (deserializer.data.attribute instanceof AsOfAttribute)
            {
                AsOfAttribute asOfAttribute = (AsOfAttribute) deserializer.data.attribute;
                if (asOfAttribute.isProcessingDate())
                {
                    deserializer.data.partial.processingDate = value;
                }
                else
                {
                    deserializer.data.partial.businessDate = value;
                }
            }
            else
            {
                ((TimestampAttribute) deserializer.data.attribute).setTimestampValue(deserializer.data.partial.dataObject, value);
            }
            attributeDone(deserializer);
        }

        @Override
        public void setTimeField(Time value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            ((TimeAttribute)deserializer.data.attribute).setTimeValue(deserializer.data.partial.dataObject, value);
            attributeDone(deserializer);
        }

        @Override
        public void skipCurrentFieldOrRelationship(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.data.currentState = InObjectState.INSTANCE;
        }
    }
    
    protected static class State
    {
        protected void commonStartState(ReladomoDeserializer deserializer) throws DeserializationException
        {
            deserializer.data.partial = new PartialDeserialized();
            deserializer.data.partial.dataObject = deserializer.data.metaData.constructData();
            deserializer.data.currentState = InObjectState.INSTANCE;
        }

        public void startObject(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call startObject in "+this.getClass().getName());
        }

        public void endObject(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call endObject in "+this.getClass().getName());
        }

        public void startList(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call startList in "+this.getClass().getName());
        }

        public void endList(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call endList in "+this.getClass().getName());
        }

        public void startListElements(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call startListElements in "+this.getClass().getName());
        }

        public void endListElements(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call endListElements in "+this.getClass().getName());
        }

        public FieldOrRelation startFieldOrRelationship(String name, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call startFieldOrRelationship in "+this.getClass().getName());
        }

        public Attribute startAttribute(String name, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call startAttribute in "+this.getClass().getName());
        }

        public FieldOrRelation startRelationship(String name, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call startRelationship in "+this.getClass().getName());
        }

        public void parseFieldFromString(String value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call parseFieldFromString in "+this.getClass().getName());
        }

        public void setFieldOrRelationshipNull(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setFieldOrRelationshipNull in "+this.getClass().getName());
        }

        public void skipCurrentFieldOrRelationship(ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call skipCurrentFieldOrRelationship in "+this.getClass().getName());
        }

        public void setReladomoObjectState(int objectState, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setByteField(byte value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setShortField(short value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setIntField(int value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setLongField(long value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setFloatField(float value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setDoubleField(double value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setBooleanField(boolean value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setCharField(char value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setByteArrayField(byte[] value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setStringField(String value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setBigDecimalField(BigDecimal value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setDateField(Date value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setTimestampField(Timestamp value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void setTimeField(Time value, ReladomoDeserializer deserializer) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void storeNested(ReladomoDeserializer deserializer, PartialDeserialized toSave) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void storeNestedList(ReladomoDeserializer deserializer, List<PartialDeserialized> toSave) throws DeserializationException
        {
            throw new DeserializationException("Should not call setReladomoObjectState in "+this.getClass().getName());
        }

        public void storeReladomoClassName(ReladomoDeserializer deserializer, String className) throws DeserializationException
        {
            throw new DeserializationException("Should not call storeReladomoClassName in "+this.getClass().getName());
        }
    }

}
