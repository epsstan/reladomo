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
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.util.Time;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public interface SerialWriter<C extends ReladomoSerializationContext>
{
    public void writeBoolean(MithraObject reladomoObject, C context, String attributeName, boolean value);
    public void writeByte(MithraObject reladomoObject, C context, String attributeName, byte value);
    public void writeShort(MithraObject reladomoObject, C context, String attributeName, short value);
    public void writeInt(MithraObject reladomoObject, C context, String attributeName, int value);
    public void writeLong(MithraObject reladomoObject, C context, String attributeName, long value);
    public void writeChar(MithraObject reladomoObject, C context, String attributeName, char value);
    public void writeFloat(MithraObject reladomoObject, C context, String attributeName, float value);
    public void writeDouble(MithraObject reladomoObject, C context, String attributeName, double value);
    public void writeByteArray(MithraObject reladomoObject, C context, String attributeName, byte[] value);
    public void writeBigDecimal(MithraObject reladomoObject, C context, String attributeName, BigDecimal value);
    public void writeTimestamp(MithraObject reladomoObject, C context, String attributeName, Timestamp value);
    public void writeDate(MithraObject reladomoObject, C context, String attributeName, Date value);
    public void writeString(MithraObject reladomoObject, C context, String attributeName, String value);
    public void writeTime(MithraObject reladomoObject, C context, String attributeName, Time value);

    public void writeObject(MithraObject reladomoObject, C context, String attributeName, Object value);

    public void writeLink(MithraObject reladomoObject, C context, String linkName, Attribute[] dependentAttributes);

    public void writeNull(MithraObject reladomoObject, C context, String attributeName, Class type);

    public void startReladomoObject(MithraObject reladomoObject, C context);
    public void endReladomoObject(MithraObject reladomoObject, C context);

    public void startRelatedObject(MithraObject reladomoObject, C context, String attributeName, AbstractRelatedFinder finder, MithraObject value);
    public void endRelatedObject(MithraObject reladomoObject, C context, String attributeName, AbstractRelatedFinder finder, MithraObject value);

    public void startRelatedReladomoList(MithraObject reladomoObject, C context, String attributeName, AbstractRelatedFinder finder, MithraList valueList);
    public void endRelatedReladomoList(MithraObject reladomoObject, C context, String attributeName, AbstractRelatedFinder finder, MithraList valueList);

    public void startMetadata(MithraObject reladomoObject, C context);
    public void writeMetadataEnd(MithraObject reladomoObject, C context);
        
    public void startAttributes(MithraObject reladomoObject, C context, int size);
    public void endAttributes(MithraObject reladomoObject, C context);

    public void startRelationships(MithraObject reladomoObject, C context, int size);
    public void endRelationships(MithraObject reladomoObject, C context);

    public void startLinks(MithraObject reladomoObject, C c, int size);
    public void endLinks(MithraObject reladomoObject, C c);

    public void startAnnotatedMethod(MithraObject reladomoObject, C context, int size);
    public void endAnnotatedMethod(MithraObject reladomoObject, C context);

    public void startReladomoList(MithraList reladomoList, C context);

    public void endReladomoList(MithraList reladomoList, C context);

    public void startReladomoListMetatdata(MithraList reladomoList, C context);

    public void endReladomoListMedatadata(MithraList reladomoList, C context);

    public void startReladomoListItem(MithraList reladomoList, C context, int index, MithraObject reladomoObject);

    public void endReladomoListItem(MithraList reladomoList, C c, int index, MithraObject reladomoObject);
}
