package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.*;
import com.gs.fw.common.mithra.generator.metamodel.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;

public class ReladomoObjectBuilder
{
    public MithraObject buildReladomoObject(ObjectResourceWrapper objectResource)
    {
        ReladomoObject reladomoObject = objectResource.getReladomoObject();

        MithraObject mithraObject = new MithraObject();
        mithraObject.setPackageName(reladomoObject.packageName());
        mithraObject.setClassName(objectResource.getName());
        mithraObject.setDefaultTable(reladomoObject.defaultTableName());
        mithraObject.setObjectType(reladomoObject.objectType().getType());

        setSuperClass(reladomoObject, mithraObject);
        setSuperClassType(reladomoObject, mithraObject);
        setUpdateListener(reladomoObject, mithraObject);
        setDatedTransactionalTemporalDirector(reladomoObject, mithraObject);

        List<AsOfAttributeType> asOfAttributeTypes = new ArrayList<AsOfAttributeType>();
        List<AttributeType> attributeTypes = new ArrayList<AttributeType>();
        List<RelationshipType> relationshipTypes = new ArrayList<RelationshipType>();
        List<IndexType> indexTypes = new ArrayList<IndexType>();

        // todo : add source attribute
        // todo : maybe convert this to use the visitor pattern ?
        AnnotationParser.ElementsByType elementsByType = partitionElements(objectResource.getEnclosedElements());
        gatherAttributes(asOfAttributeTypes, attributeTypes, elementsByType.attributes);
        gatherRelationships(relationshipTypes, elementsByType.relationships);
        gatherIndices(indexTypes, elementsByType.indices);

        mithraObject.setAsOfAttributes(asOfAttributeTypes);
        mithraObject.setAttributes(attributeTypes);
        mithraObject.setRelationships(relationshipTypes);
        return mithraObject;
    }

    private void setUpdateListener(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one listener class can be specified
        Class[] updateListenerClasses = reladomoObject.updateListenerClass();
        if (updateListenerClasses != null && updateListenerClasses.length == 1)
        {
            mithraObject.setUpdateListener(updateListenerClasses[0].getCanonicalName());
        }
    }

    private void setDatedTransactionalTemporalDirector(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one listener class can be specified
        Class[] directorClasses = reladomoObject.datedTransactionalTemporalDirector();
        if (directorClasses != null && directorClasses.length == 1)
        {
            mithraObject.setDatedTransactionalTemporalDirector(directorClasses[0].getCanonicalName());
        }
    }

    private void setSuperClass(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one superclass can be specified
        SuperClass[] superClasses = reladomoObject.superClass();
        if (superClasses != null && superClasses.length == 1)
        {
            SuperClass superClass = superClasses[0];
            SuperClassAttributeType superClassAttributeType = new SuperClassAttributeType();
            superClassAttributeType.setName(superClass.name());
            superClassAttributeType.setGenerated(superClass.generated());
            mithraObject.setSuperClass(superClassAttributeType);
        }
    }

    private void setSuperClassType(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one superclass can be specified
        SuperClassType.Enums[] superClassTypes = reladomoObject.superClassType();
        if (superClassTypes != null && superClassTypes.length == 1)
        {
            mithraObject.setSuperClassType(superClassTypes[0].getType());
        }
    }

    private void gatherRelationships(List<RelationshipType> relationshipTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            relationshipTypes.add(makeRelationship(element, name));
        }
    }

    private void gatherIndices(List<IndexType> indexTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            indexTypes.add(makeIndexType(element, name));
        }
    }

    private void gatherAttributes(List<AsOfAttributeType> asOfAttributeTypes, List<AttributeType> attributeTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            if (isAsOfAttribute(element))
            {
                addAsOfAttribute(element, name, asOfAttributeTypes);
            } else
            {
                addAttribute(element, name, attributeTypes);
            }
        }
    }

    private AnnotationParser.ElementsByType partitionElements(List<? extends Element> elements)
    {
        AnnotationParser.ElementsByType elementsByType = new AnnotationParser.ElementsByType();
        for (Element element : elements)
        {
            if (isAttribute(element))
            {
                elementsByType.addAttribute(element);
            }
            else if (isRelationship(element))
            {
                elementsByType.addRelationship(element);
            }
            else if (isIndex(element))
            {
                elementsByType.addIndex(element);
            }
        }
        return elementsByType;
    }

    private boolean isAttribute(Element element)
    {
        for (Class clazz : new Class[]{
                BigDecimalAttribute.class, ByteArrayAttribute.class, ByteAttribute.class,
                CharAttribute.class, DoubleAttribute.class, FloatAttribute.class,
                IntAttribute.class, LongAttribute.class, StringAttribute.class})
        {
            if (element.getAnnotation(clazz) != null)
            {
                return true;
            }
        }
        return false;
    }

    private boolean isRelationship(Element element)
    {
        return element.getAnnotation(Relationship.class) != null;
    }

    private boolean isIndex(Element element)
    {
        return element.getAnnotation(Index.class) != null;
    }

    private boolean isAsOfAttribute(Element element)
    {
        return element.getAnnotation(AsOfAttribute.class) != null;
    }

    private void addAsOfAttribute(Element element, String name, List<AsOfAttributeType> asOfAttributeTypes)
    {
        AsOfAttribute asOfAttribute = element.getAnnotation(AsOfAttribute.class);
        if (asOfAttribute != null)
        {
            AsOfAttributeType attributeType = makeAsOfAttribute(asOfAttribute, name);
            asOfAttributeTypes.add(attributeType);
            Properties properties = element.getAnnotation(Properties.class);
            if (properties != null)
            {
                List<PropertyType> propertyTypes = extractProperties(properties);
                attributeType.setProperties(propertyTypes);
            }
        }
    }

    private void addAttribute(Element element, String name, List<AttributeType> attributeTypes)
    {
        AttributeType attributeType = makeTypedAttribute(element, name);
        if (attributeType == null)
        {
            // todo : generate validation error
        } else
        {
            setPrimaryKeyStrategy(element, attributeType);
            attributeTypes.add(attributeType);

            Properties properties = element.getAnnotation(Properties.class);
            if (properties != null)
            {
                List<PropertyType> propertyTypes = extractProperties(properties);
                attributeType.setProperties(propertyTypes);
            }
        }
    }

    private AttributeType makeTypedAttribute(Element reladomoObjectSpecElement, String name)
    {
        ByteAttribute byteAttribute = reladomoObjectSpecElement.getAnnotation(ByteAttribute.class);
        if (byteAttribute != null)
        {
            return makeByteAttribute(byteAttribute, name);
        }
        CharAttribute charAttribute = reladomoObjectSpecElement.getAnnotation(CharAttribute.class);
        if (charAttribute != null)
        {
            return makeCharAttribute(charAttribute, name);
        }
        IntAttribute intAttribute = reladomoObjectSpecElement.getAnnotation(IntAttribute.class);
        if (intAttribute != null)
        {
            return makeIntAttribute(intAttribute, name);
        }
        LongAttribute longAttribute = reladomoObjectSpecElement.getAnnotation(LongAttribute.class);
        if (longAttribute != null)
        {
            return makeLongAttribute(longAttribute, name);
        }
        FloatAttribute floatAttribute = reladomoObjectSpecElement.getAnnotation(FloatAttribute.class);
        if (floatAttribute != null)
        {
            return makeFloatAttribute(floatAttribute, name);
        }
        DoubleAttribute doubleAttribute = reladomoObjectSpecElement.getAnnotation(DoubleAttribute.class);
        if (doubleAttribute != null)
        {
            return makeDoubleAttribute(doubleAttribute, name);
        }
        BigDecimalAttribute bigDecimalAttribute = reladomoObjectSpecElement.getAnnotation(BigDecimalAttribute.class);
        if (bigDecimalAttribute != null)
        {
            return makeBigDecimalAttribute(bigDecimalAttribute, name);
        }
        StringAttribute stringAttribute = reladomoObjectSpecElement.getAnnotation(StringAttribute.class);
        if (stringAttribute != null)
        {
            return makeStringAttribute(stringAttribute, name);
        }
        ByteArrayAttribute byteArrayAttribute = reladomoObjectSpecElement.getAnnotation(ByteArrayAttribute.class);
        if (byteArrayAttribute != null)
        {
            return makeByteArrayAttribute(byteArrayAttribute, name);
        }
        return null;
    }

    private AttributeType makeCharAttribute(CharAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setDefaultIfNull(spec.defaultIfNull());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("char");

        return attributeType;
    }

    private AttributeType makeByteAttribute(ByteAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setDefaultIfNull(spec.defaultIfNull());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("byte");

        return attributeType;
    }

    private AttributeType makeByteArrayAttribute(ByteArrayAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("byte[]");

        return attributeType;
    }

    private AttributeType makeStringAttribute(StringAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("String");
        attributeType.setMaxLength(spec.maxLength());
        attributeType.setTruncate(spec.truncate());
        attributeType.setTrim(spec.trim());
        attributeType.setPoolable(spec.poolable());

        return attributeType;
    }

    private void setPrimaryKeyStrategy(Element element, AttributeType attributeType)
    {
        PrimaryKey primaryKey = element.getAnnotation(PrimaryKey.class);
        if (primaryKey == null)
        {
            attributeType.setPrimaryKey(false);
            return;
        }
        attributeType.setPrimaryKey(true);
        attributeType.setMutablePrimaryKey(primaryKey.mutable());
        boolean strategyAdded = addPKStrategy(element, attributeType);
        if (!strategyAdded)
        {
            //todo : generate validation error
        }
    }

    private boolean addPKStrategy(Element element, AttributeType attributeType)
    {
        SimulatedSequencePKStrategy simulatedSequencePKStrategy = element.getAnnotation(SimulatedSequencePKStrategy.class);
        if (simulatedSequencePKStrategy != null)
        {
            SimulatedSequenceType simulatedSequenceType = new SimulatedSequenceType();
            simulatedSequenceType.setBatchSize(simulatedSequencePKStrategy.batchSize());
            simulatedSequenceType.setInitialValue(simulatedSequencePKStrategy.intialValue());
            simulatedSequenceType.setIncrementSize(simulatedSequencePKStrategy.incrementSize());
            simulatedSequenceType.setSequenceName(simulatedSequencePKStrategy.sequenceName());
            simulatedSequenceType.setSequenceObjectFactoryName(simulatedSequencePKStrategy.sequenceObjectFactoryName());
            simulatedSequenceType.setHasSourceAttribute(simulatedSequencePKStrategy.hasSourceAttribute());

            attributeType.setSimulatedSequence(simulatedSequenceType);
            attributeType.setPrimaryKeyGeneratorStrategy(PrimaryKeyGeneratorStrategyType.SimulatedSequence);
            return true;
        }
        MaxPKStrategy maxPKStrategy = element.getAnnotation(MaxPKStrategy.class);
        if (maxPKStrategy != null)
        {
            attributeType.setPrimaryKeyGeneratorStrategy(PrimaryKeyGeneratorStrategyType.Max);
            return true;
        }
        return false;
    }

    private AttributeType makeIntAttribute(IntAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("int");
        attributeType.setUseForOptimisticLocking(spec.useForOptimisticLocking());

        //todo : is properties applicable ?
        return attributeType;
    }

    private AttributeType makeLongAttribute(LongAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("long");
        attributeType.setUseForOptimisticLocking(spec.useForOptimisticLocking());

        //todo : is properties applicable ?
        return attributeType;
    }

    //((AnnotationInvocationHandler) ((Proxy) spec).getInvocationHandler()).getMemberMethods()[0]
    private AttributeType makeDoubleAttribute(DoubleAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("double");

        //todo : is properties applicable ?
        return attributeType;
    }

    private AttributeType makeFloatAttribute(FloatAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("float");

        //todo : is properties applicable ?
        return attributeType;
    }

    private AttributeType makeBigDecimalAttribute(BigDecimalAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("BigDecimal");
        attributeType.setPrecision(spec.precision());
        attributeType.setScale(spec.scale());

        //todo : is properties applicable ?
        return attributeType;
    }

    private AsOfAttributeType makeAsOfAttribute(AsOfAttribute spec, String name)
    {
        AsOfAttributeType asOfAttributeType = new AsOfAttributeType();
        asOfAttributeType.setName(name);
        asOfAttributeType.setSetAsString(spec.setAsString());
        asOfAttributeType.setFromColumnName(spec.fromColumnName());
        asOfAttributeType.setToColumnName(spec.toColumnName());
        asOfAttributeType.setFinalGetter(spec.finalGetters());
        asOfAttributeType.setDefaultIfNotSpecified(spec.defaultIfNotSpecified());
        asOfAttributeType.setFutureExpiringRowsExist(spec.futureExpiringRowsExist());
        asOfAttributeType.setInfinityDate(spec.infinityDate());
        asOfAttributeType.setInfinityIsNull(spec.infinityIsNull());
        asOfAttributeType.setIsProcessingDate(spec.isProcessingDate());
        asOfAttributeType.setToIsInclusive(spec.toIsInclusive());
        asOfAttributeType.setTimezoneConversion(spec.timezoneConversion().getType());
        asOfAttributeType.setTimestampPrecision(spec.timestampPrecision().getType());
        asOfAttributeType.setPoolable(spec.poolable());
        return asOfAttributeType;
    }

    private RelationshipType makeRelationship(Element element, String name)
    {
        Relationship spec = element.getAnnotation(Relationship.class);
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        String relatedObject = returnType.asElement().getSimpleName().toString().replaceAll("Spec", "");

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setName(name);
        relationshipType.setRelatedIsDependent(spec.relatedIsDependent());
        relationshipType.setCardinality(spec.cardinality().getType());
        relationshipType.setRelatedObject(relatedObject);
        relationshipType._setValue(spec.contract());
        relationshipType.setForeignKey(spec.foreignKeyType().getType());
        relationshipType.setDirectReference(spec.directReference());
        relationshipType.setFinalGetter(spec.finalGetter());
        if (!spec.reverseRelationshipName().trim().isEmpty())
        {
            relationshipType.setReverseRelationshipName(spec.reverseRelationshipName());
        }
        if (!spec.orderBy().trim().isEmpty())
        {
            relationshipType.setOrderBy(spec.orderBy());
        }
        if (!spec.parameters().isEmpty())
        {
            relationshipType.setParameters(spec.parameters());
        }
        if (!spec.returnType().isEmpty())
        {
            relationshipType.setReturnType(spec.returnType());
        }
        return relationshipType;
    }

    private IndexType makeIndexType(Element element, String name)
    {
        Index spec = element.getAnnotation(Index.class);
        IndexType indexType = new IndexType();
        indexType.setName(name);
        indexType.setUnique(spec.unique());
        indexType._setValue(spec.attributes());
        return indexType;
    }

    private List<PropertyType> extractProperties(Properties spec)
    {
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (Property property : spec.value())
        {
            PropertyType propType = new PropertyType();
            propType.setKey("\"" + property.key() + "\"");
            propType.setValue("\"" + property.value() + "\"");
            propertyTypes.add(propType);
        }
        return propertyTypes;
    }

}
