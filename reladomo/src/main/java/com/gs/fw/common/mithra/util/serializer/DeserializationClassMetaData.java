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

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import com.gs.fw.common.mithra.MithraDataObject;
import com.gs.fw.common.mithra.attribute.AsOfAttribute;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.finder.RelatedFinder;

import java.lang.reflect.Method;

public class DeserializationClassMetaData
{
    private final RelatedFinder relatedFinder;
    private final Class businessClass;
    private final Class dataClass;
    private final ObjectIntHashMap<Attribute> attributePosition = new ObjectIntHashMap<Attribute>();

    public DeserializationClassMetaData(RelatedFinder relatedFinder) throws RuntimeException
    {
        this.relatedFinder = ((AbstractRelatedFinder) relatedFinder).zWithoutParentSelector();
        String finderClassName = relatedFinder.getFinderClassName();
        String className = finderClassName.substring(0, finderClassName.length() - "Finder".length());
        try
        {
            businessClass = Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Could not get class "+className, e);
        }
        String dataClassName = className+"Data";
        try
        {
            Class dataClassTmp = Class.forName(dataClassName);
            if (dataClassTmp.isInterface())
            {
                dataClassTmp = Class.forName(dataClassTmp.getName()+"$"+dataClassTmp.getSimpleName()+"OnHeap");
            }
            this.dataClass = dataClassTmp;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Could not get class "+className, e);
        }
        int count = 0;
        for(Attribute attr: this.relatedFinder.getPersistentAttributes())
        {
            attributePosition.put(attr, count);
            count++;
        }
        AsOfAttribute[] asOfAttributes = this.relatedFinder.getAsOfAttributes();
        if (asOfAttributes != null)
        {
            for(AsOfAttribute attr: asOfAttributes)
            {
                attributePosition.put(attr, count);
                count++;
            }
        }
    }

    public MithraDataObject constructData() throws DeserializationException
    {
        try
        {
            return (MithraDataObject) dataClass.newInstance();
        }
        catch (Exception e)
        {
            throw new DeserializationException("Could not instantiate object for class "+dataClass.getName(), e);
        }
    }

    public Attribute getAttributeByName(String attributeName)
    {
        return relatedFinder.getAttributeByName(attributeName);
    }

    public RelatedFinder getRelationshipFinderByName(String relationshipName)
    {
        return relatedFinder.getRelationshipFinderByName(relationshipName);
    }

    public Method getAnnotatedMethodByName(String name)
    {
        return DeserializableMethodCache.getInstance().get(businessClass, name);
    }

    public int getTotalAttributes()
    {
        return attributePosition.size();
    }

    public int getAttriutePosition(Attribute attribute)
    {
        return attributePosition.getIfAbsent(attribute, -1);
    }
}
