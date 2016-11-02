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

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraObject;
import com.gs.fw.common.mithra.MithraTransactionalObject;
import com.gs.fw.common.mithra.attribute.*;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.Mapper;
import com.gs.fw.common.mithra.util.Time;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ReladomoSerializationContext
{
    public static final String RELADOMO_PREFIX = "_rdo";
    public static final String RELADOMO_CLASS_NAME = RELADOMO_PREFIX + "ClassName";
    public static final String RELADOMO_STATE = RELADOMO_PREFIX + "State";
    public static final String RELADOMO_LIST_SIZE = RELADOMO_PREFIX + "ListSize";

    public static final byte READ_ONLY_STATE = 1;
    public static final byte DETACHED_STATE = 2;
    public static final byte IN_MEMORY_STATE = 3;

    protected final SerializationConfig serializationConfig;
    protected final SerialWriter writer;

    protected SerializationNode currentNode;

    public ReladomoSerializationContext(SerializationConfig serializationConfig, SerialWriter writer)
    {
        this.serializationConfig = serializationConfig;
        this.currentNode = this.serializationConfig.getRootNode();
        this.writer = writer;
    }

    public void serializeReladomoObject(MithraObject reladomoObject)
    {
        writer.startReladomoObject(reladomoObject, this);
        
        writer.startMetadata(reladomoObject, this);
        serializeClassMetadata(reladomoObject);
        writer.writeMetadataEnd(reladomoObject, this);
        
        writer.startAttributes(reladomoObject, this, this.currentNode.getAttributes().size());
        serializeAttributes(reladomoObject);
        writer.endAttributes(reladomoObject, this);
        
        writer.startRelationships(reladomoObject, this, this.currentNode.getChildren().size());
        serializeRelationships(reladomoObject);
        writer.endRelationships(reladomoObject, this);

        writer.startLinks(reladomoObject, this, this.currentNode.getLinks().size());
        serializeLinks(reladomoObject);
        writer.endLinks(reladomoObject, this);

        List<Method> annotatedMethods = getAnnotatedMethods(reladomoObject);
        writer.startAnnotatedMethod(reladomoObject, this, annotatedMethods.size());
        serializeAnnotatedMethods(reladomoObject, annotatedMethods);
        writer.endAnnotatedMethod(reladomoObject, this);

        writer.endReladomoObject(reladomoObject, this);
    }

    protected List<Method> getAnnotatedMethods(MithraObject reladomoObject)
    {
        return this.serializationConfig.getAnnotatedMethods(reladomoObject.getClass());
    }

    protected void serializeLinks(MithraObject reladomoObject)
    {
        List<SerializationNode> links = this.currentNode.getLinks();
        for(int i=0;i<links.size();i++)
        {
            SerializationNode linkNode = links.get(i);
            writer.writeLink(reladomoObject, this, linkNode.getRelatedFinder().getRelationshipName(), linkNode.getLinkAttributes());
        }
    }

    protected void serializeAnnotatedMethods(MithraObject reladomoObject, List<Method> annotatedMethods)
    {
        for(int i=0;i<annotatedMethods.size();i++)
        {
            Method method = annotatedMethods.get(i);
            try
            {
                Object methodResult = method.invoke(reladomoObject, null);
                if (methodResult == null)
                {
                    writer.writeNull(reladomoObject, this, getMethodAttributeName(method), method.getReturnType());
                }
                else
                {
                    writeByType(reladomoObject, methodResult, getMethodAttributeName(method));
                }
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException("Could not call method "+method.getName()+" on class "+reladomoObject.getClass().getName(), e);
            }
            catch (InvocationTargetException e)
            {
                throw new RuntimeException("Could not call method "+method.getName()+" on class "+reladomoObject.getClass().getName(), e);
            }
        }
    }

    private void writeByType(MithraObject reladomoObject, Object methodResult, String methodAttributeName)
    {
        if (methodResult instanceof Boolean)
        {
            writer.writeBoolean(reladomoObject, this, methodAttributeName, ((Boolean) methodResult));
        }
        else if (methodResult instanceof Byte)
        {
            writer.writeByte(reladomoObject, this, methodAttributeName, ((Byte) methodResult));
        }
        else if (methodResult instanceof Short)
        {
            writer.writeShort(reladomoObject, this, methodAttributeName, ((Short) methodResult));
        }
        else if (methodResult instanceof Integer)
        {
            writer.writeInt(reladomoObject, this, methodAttributeName, ((Integer) methodResult));
        }
        else if (methodResult instanceof Long)
        {
            writer.writeByte(reladomoObject, this, methodAttributeName, ((Byte) methodResult));
        }
        else if (methodResult instanceof Character)
        {
            writer.writeChar(reladomoObject, this, methodAttributeName, ((Character) methodResult));
        }
        else if (methodResult instanceof Float)
        {
            writer.writeFloat(reladomoObject, this, methodAttributeName, ((Float) methodResult));
        }
        else if (methodResult instanceof Double)
        {
            writer.writeDouble(reladomoObject, this, methodAttributeName, ((Double) methodResult));
        }
        else if (methodResult instanceof byte[])
        {
            writer.writeByteArray(reladomoObject, this, methodAttributeName, ((byte[]) methodResult));
        }
        else if (methodResult instanceof BigDecimal)
        {
            writer.writeBigDecimal(reladomoObject, this, methodAttributeName, ((BigDecimal) methodResult));
        }
        else if (methodResult instanceof Timestamp)
        {
            writer.writeTimestamp(reladomoObject, this, methodAttributeName, ((Timestamp) methodResult));
        }
        else if (methodResult instanceof Date)
        {
            writer.writeDate(reladomoObject, this, methodAttributeName, ((Date) methodResult));
        }
        else if (methodResult instanceof String)
        {
            writer.writeString(reladomoObject, this, methodAttributeName, ((String) methodResult));
        }
        else if (methodResult instanceof Time)
        {
            writer.writeTime(reladomoObject, this, methodAttributeName, ((Time) methodResult));
        }
        else
        {
            writer.writeObject(reladomoObject, this, methodAttributeName, methodResult);
        }
    }

    private String getMethodAttributeName(Method method)
    {
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 4 && Character.isUpperCase(name.charAt(3)))
        {
            return Character.toLowerCase(name.charAt(3))+name.substring(4);
        }
        return name;
    }

    protected void serializeRelationships(MithraObject reladomoObject)
    {
        SerializationNode pushedNode = this.currentNode;
        List<SerializationNode> children = this.currentNode.getChildren();
        for(int i=0;i<children.size();i++)
        {
            SerializationNode serializationNode = children.get(i);
            AbstractRelatedFinder relatedFinder = serializationNode.getRelatedFinder();
            this.currentNode = serializationNode;
            if (relatedFinder.isToOne())
            {
                MithraObject related = (MithraObject) relatedFinder.valueOf(reladomoObject);
                this.writer.startRelatedObject(reladomoObject, this, relatedFinder.getRelationshipName(), relatedFinder, related);
                this.serializeReladomoObject(related);
                this.writer.endRelatedObject(reladomoObject, this, relatedFinder.getRelationshipName(), relatedFinder, related);
            }
            else
            {
                MithraList relatedList = (MithraList) relatedFinder.listValueOf(reladomoObject);
                this.writer.startRelatedReladomoList(reladomoObject, this, relatedFinder.getRelationshipName(), relatedFinder, relatedList);
                this.serializeReladomoList(relatedList);
                this.writer.endRelatedReladomoList(reladomoObject, this, relatedFinder.getRelationshipName(), relatedFinder, relatedList);
            }
        }
        this.currentNode = pushedNode;
    }

    protected void serializeAttributes(MithraObject reladomoObject)
    {
        List<Attribute> attributes = this.currentNode.getAttributes();
        for(int i=0;i<attributes.size();i++)
        {
            attributes.get(i).zWriteSerial(this, this.writer, reladomoObject);
        }
    }

    protected void serializeClassMetadata(MithraObject reladomoObject)
    {
        writer.writeString(reladomoObject, this, RELADOMO_CLASS_NAME, reladomoObject.getClass().getName());
        byte state;
        if (reladomoObject instanceof MithraTransactionalObject)
        {
            MithraTransactionalObject txObj = (MithraTransactionalObject) reladomoObject;
            if (txObj.zIsDetached())
            {
                state = DETACHED_STATE;
            }
            else if (txObj.isInMemoryAndNotInserted() || txObj.isInMemoryNonTransactional())
            {
                state = IN_MEMORY_STATE;
            }
            else
            {
                state = DETACHED_STATE; // the object is in persisted state, but we're serializing as detached.
            }
        }
        else
        {
            state = READ_ONLY_STATE;
        }
        writer.writeByte(reladomoObject, this, RELADOMO_STATE, state);
    }

    public void serializeReladomoList(MithraList reladomoList)
    {
        writer.startReladomoList(reladomoList, this);

        writer.startReladomoListMetatdata(reladomoList, this);
        writer.writeString(null, this, RELADOMO_CLASS_NAME, getCurrentClassName());
        writer.writeInt(null, this, RELADOMO_LIST_SIZE, reladomoList.size());
        writer.endReladomoListMedatadata(reladomoList, this);

        for(int i=0;i<reladomoList.size();i++)
        {
            MithraObject reladomoObject = (MithraObject) reladomoList.get(i);
            writer.startReladomoListItem(reladomoList, this, i, reladomoObject);
            serializeReladomoObject(reladomoObject);
            writer.endReladomoListItem(reladomoList, this, i, reladomoObject);
        }

        writer.endReladomoList(reladomoList, this);
    }

    protected String getCurrentClassName()
    {
        String finderClassName = this.currentNode.getRelatedFinder().getFinderClassName();
        return finderClassName.substring(0, finderClassName.length() - "Finder".length());
    }

}
