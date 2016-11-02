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

package com.gs.reladomo.serial.gson;

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraObject;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.util.Time;
import com.gs.fw.common.mithra.util.serializer.SerialWriter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class GsonReladomoSerialWriter implements SerialWriter<GsonRelodomoSerialContext>
{
    @Override
    public void writeBoolean(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, boolean value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeByte(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, byte value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeShort(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, short value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeInt(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, int value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeLong(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, long value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeChar(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, char value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeFloat(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, float value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeDouble(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, double value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeByteArray(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, byte[] value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeBigDecimal(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, BigDecimal value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeTimestamp(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, Timestamp value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeDate(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, Date value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeString(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, String value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeTime(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, Time value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeObject(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, Object value)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, value);
    }

    @Override
    public void writeLink(MithraObject reladomoObject, GsonRelodomoSerialContext context, String linkName, Attribute[] dependentAttributes)
    {
        //todo implement link
    }

    @Override
    public void writeNull(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, Class type)
    {
        context.getCurrentResultAsObject().addProperty(attributeName, (String) null);
    }

    @Override
    public void startReladomoObject(MithraObject reladomoObject, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void endReladomoObject(MithraObject reladomoObject, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void startRelatedObject(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraObject value)
    {
        context.pushNewObject(attributeName);
    }

    @Override
    public void endRelatedObject(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraObject value)
    {
        context.pop();
    }

    @Override
    public void startRelatedReladomoList(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraList valueList)
    {

    }

    @Override
    public void endRelatedReladomoList(MithraObject reladomoObject, GsonRelodomoSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraList valueList)
    {

    }

    @Override
    public void startMetadata(MithraObject reladomoObject, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void writeMetadataEnd(MithraObject reladomoObject, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void startAttributes(MithraObject reladomoObject, GsonRelodomoSerialContext context, int size)
    {

    }

    @Override
    public void endAttributes(MithraObject reladomoObject, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void startRelationships(MithraObject reladomoObject, GsonRelodomoSerialContext context, int size)
    {

    }

    @Override
    public void endRelationships(MithraObject reladomoObject, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void startLinks(MithraObject reladomoObject, GsonRelodomoSerialContext gsonRelodomoSerialContext, int size)
    {

    }

    @Override
    public void endLinks(MithraObject reladomoObject, GsonRelodomoSerialContext gsonRelodomoSerialContext)
    {

    }

    @Override
    public void startAnnotatedMethod(MithraObject reladomoObject, GsonRelodomoSerialContext context, int size)
    {

    }

    @Override
    public void endAnnotatedMethod(MithraObject reladomoObject, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void startReladomoList(MithraList reladomoList, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void endReladomoList(MithraList reladomoList, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void startReladomoListMetatdata(MithraList reladomoList, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void endReladomoListMedatadata(MithraList reladomoList, GsonRelodomoSerialContext context)
    {

    }

    @Override
    public void startReladomoListItem(MithraList reladomoList, GsonRelodomoSerialContext context, int index, MithraObject reladomoObject)
    {

    }

    @Override
    public void endReladomoListItem(MithraList reladomoList, GsonRelodomoSerialContext gsonRelodomoSerialContext, int index, MithraObject reladomoObject)
    {

    }
}
