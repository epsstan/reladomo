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

package com.gs.reladomo.serial.jackson;

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraObject;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.util.Time;
import com.gs.fw.common.mithra.util.serializer.SerialWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class JacksonReladomoSerialWriter implements SerialWriter<JacksonReladomoSerialContext>
{
    @Override
    public void writeBoolean(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, boolean value) throws IOException
    {
        context.getJgen().writeBooleanField(attributeName, value);
    }

    @Override
    public void writeByte(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, byte value) throws IOException
    {
        context.getJgen().writeNumberField(attributeName, value);
    }

    @Override
    public void writeShort(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, short value) throws IOException
    {
        context.getJgen().writeNumberField(attributeName, value);
    }

    @Override
    public void writeInt(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, int value) throws IOException
    {
        context.getJgen().writeNumberField(attributeName, value);
    }

    @Override
    public void writeLong(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, long value) throws IOException
    {
        context.getJgen().writeNumberField(attributeName, value);
    }

    @Override
    public void writeChar(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, char value) throws IOException
    {
        context.getJgen().writeStringField(attributeName, ""+value);
    }

    @Override
    public void writeFloat(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, float value) throws IOException
    {
        context.getJgen().writeNumberField(attributeName, value);
    }

    @Override
    public void writeDouble(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, double value) throws IOException
    {
        context.getJgen().writeNumberField(attributeName, value);
    }

    @Override
    public void writeByteArray(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, byte[] value) throws IOException
    {
        context.getJgen().writeBinaryField(attributeName, value);
    }

    @Override
    public void writeBigDecimal(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, BigDecimal value) throws IOException
    {
        context.getJgen().writeNumberField(attributeName, value);
    }

    @Override
    public void writeTimestamp(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, Timestamp value) throws IOException
    {
        context.getJgen().writeObjectField(attributeName, value);
    }

    @Override
    public void writeDate(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, Date value) throws IOException
    {
        context.getJgen().writeObjectField(attributeName, value);
    }

    @Override
    public void writeString(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, String value) throws IOException
    {
        context.getJgen().writeStringField(attributeName, value);
    }

    @Override
    public void writeTime(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, Time value) throws IOException
    {
        context.getJgen().writeStringField(attributeName, value.toString());
    }

    @Override
    public void writeObject(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, Object value) throws IOException
    {
        context.getJgen().writeObjectField(attributeName, value);
    }

    @Override
    public void writeLink(MithraObject reladomoObject, JacksonReladomoSerialContext context, String linkName, Attribute[] dependentAttributes) throws IOException
    {
        //todo
    }

    @Override
    public void writeNull(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, Class type) throws IOException
    {
        context.getJgen().writeNullField(attributeName);
    }

    @Override
    public void startReladomoObject(MithraObject reladomoObject, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeStartObject();
    }

    @Override
    public void endReladomoObject(MithraObject reladomoObject, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeEndObject();
    }

    @Override
    public void startRelatedObject(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraObject value) throws IOException
    {
        context.getJgen().writeFieldName(attributeName);
    }

    @Override
    public void endRelatedObject(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraObject value) throws IOException
    {
    }

    @Override
    public void startRelatedReladomoList(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraList valueList) throws IOException
    {
        context.getJgen().writeFieldName(attributeName);
        context.getJgen().writeStartObject();
    }

    @Override
    public void endRelatedReladomoList(MithraObject reladomoObject, JacksonReladomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraList valueList) throws IOException
    {
        context.getJgen().writeEndObject();
    }

    @Override
    public void startMetadata(MithraObject reladomoObject, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeObjectFieldStart("_rdoMetaData");
    }

    @Override
    public void writeMetadataEnd(MithraObject reladomoObject, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeEndObject();
    }

    @Override
    public void startAttributes(MithraObject reladomoObject, JacksonReladomoSerialContext context, int size) throws IOException
    {

    }

    @Override
    public void endAttributes(MithraObject reladomoObject, JacksonReladomoSerialContext context) throws IOException
    {

    }

    @Override
    public void startRelationships(MithraObject reladomoObject, JacksonReladomoSerialContext context, int size) throws IOException
    {

    }

    @Override
    public void endRelationships(MithraObject reladomoObject, JacksonReladomoSerialContext context) throws IOException
    {

    }

    @Override
    public void startLinks(MithraObject reladomoObject, JacksonReladomoSerialContext jacksonReladomoSerialContext, int size) throws IOException
    {

    }

    @Override
    public void endLinks(MithraObject reladomoObject, JacksonReladomoSerialContext jacksonReladomoSerialContext) throws IOException
    {

    }

    @Override
    public void startAnnotatedMethod(MithraObject reladomoObject, JacksonReladomoSerialContext context, int size) throws IOException
    {

    }

    @Override
    public void endAnnotatedMethod(MithraObject reladomoObject, JacksonReladomoSerialContext context) throws IOException
    {

    }

    @Override
    public void startReladomoList(MithraList reladomoList, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeStartObject();
    }

    @Override
    public void endReladomoList(MithraList reladomoList, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeEndObject();
    }

    @Override
    public void startReladomoListMetatdata(MithraList reladomoList, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeObjectFieldStart("_rdoMetaData");
    }

    @Override
    public void endReladomoListMedatadata(MithraList reladomoList, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeEndObject();
    }

    @Override
    public void startReladomoListItem(MithraList reladomoList, JacksonReladomoSerialContext context, int index, MithraObject reladomoObject) throws IOException
    {
//        context.getJgen().writeStartObject();
    }

    @Override
    public void endReladomoListItem(MithraList reladomoList, JacksonReladomoSerialContext context, int index, MithraObject reladomoObject) throws IOException
    {
//        context.getJgen().writeEndObject();
    }

    @Override
    public void startReladomoListElements(MithraList reladomoList, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeArrayFieldStart("elements");
    }

    @Override
    public void endReladomoListElements(MithraList reladomoList, JacksonReladomoSerialContext context) throws IOException
    {
        context.getJgen().writeEndArray();
    }
}
