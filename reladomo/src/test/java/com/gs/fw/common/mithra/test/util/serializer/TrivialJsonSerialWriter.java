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

package com.gs.fw.common.mithra.test.util.serializer;

import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.MithraObject;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.AbstractRelatedFinder;
import com.gs.fw.common.mithra.util.Time;
import com.gs.fw.common.mithra.util.serializer.SerialWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TrivialJsonSerialWriter implements SerialWriter<AppendableSerialContext>
{
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    static
    {
        TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static String timestampToJson(Timestamp timestamp)
    {
        synchronized (TIMESTAMP_FORMAT)
        {
            return TIMESTAMP_FORMAT.format(timestamp);
        }
    }

    private static String dateToJson(Date timestamp)
    {
        synchronized (DATE_FORMAT)
        {
            return DATE_FORMAT.format(timestamp);
        }
    }

    private void startField(AppendableSerialContext context, String attributeName) throws IOException
    {
        context.startField();
        context.getAppendable().append('"').append(attributeName).append('"').append(':').append(' ');
    }

    @Override
    public void writeBoolean(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, boolean value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeByte(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, byte value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeShort(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, short value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeInt(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, int value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeLong(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, long value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeChar(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, char value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append('"').append(value).append('"');
    }

    @Override
    public void writeFloat(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, float value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeDouble(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, double value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeByteArray(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, byte[] value) throws IOException
    {
        //todo
    }

    @Override
    public void writeBigDecimal(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, BigDecimal value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeTimestamp(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, Timestamp value) throws IOException
    {
        writeString(reladomoObject, context, attributeName, timestampToJson(value));
    }

    @Override
    public void writeDate(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, Date value) throws IOException
    {
        writeString(reladomoObject, context, attributeName, dateToJson(value));
    }

    @Override
    public void writeString(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, String value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append('"').append(value).append('"');
    }

    @Override
    public void writeTime(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, Time value) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append(String.valueOf(value));
    }

    @Override
    public void writeObject(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, Object value) throws IOException
    {
        //todo
    }

    @Override
    public void writeLink(MithraObject reladomoObject, AppendableSerialContext context, String linkName, Attribute[] dependentAttributes) throws IOException
    {
        //todo
    }

    @Override
    public void writeNull(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, Class type) throws IOException
    {
        startField(context, attributeName);
        context.getAppendable().append("null");
    }

    @Override
    public void startReladomoObject(MithraObject reladomoObject, AppendableSerialContext context) throws IOException
    {
        context.startObject();
        context.getAppendable().append('{');
    }

    @Override
    public void endReladomoObject(MithraObject reladomoObject, AppendableSerialContext context) throws IOException
    {
        context.getAppendable().append('}');
        context.endObject();
    }

    @Override
    public void startRelatedObject(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraObject value) throws IOException
    {
        startField(context, attributeName);
    }

    @Override
    public void endRelatedObject(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraObject value) throws IOException
    {

    }

    @Override
    public void startRelatedReladomoList(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraList valueList) throws IOException
    {
        startField(context, attributeName);
        context.startObject();
        context.getAppendable().append('{');
    }

    @Override
    public void endRelatedReladomoList(MithraObject reladomoObject, AppendableSerialContext context, String attributeName, AbstractRelatedFinder finder, MithraList valueList) throws IOException
    {
        context.getAppendable().append('}');
        context.endObject();
    }

    @Override
    public void startMetadata(MithraObject reladomoObject, AppendableSerialContext context) throws IOException
    {
        startField(context, "_rdoMetaData");
        context.startObject();
        context.getAppendable().append('{');
    }

    @Override
    public void writeMetadataEnd(MithraObject reladomoObject, AppendableSerialContext context) throws IOException
    {
        context.getAppendable().append('}');
        context.endObject();
    }

    @Override
    public void startAttributes(MithraObject reladomoObject, AppendableSerialContext context, int size) throws IOException
    {

    }

    @Override
    public void endAttributes(MithraObject reladomoObject, AppendableSerialContext context) throws IOException
    {

    }

    @Override
    public void startRelationships(MithraObject reladomoObject, AppendableSerialContext context, int size) throws IOException
    {

    }

    @Override
    public void endRelationships(MithraObject reladomoObject, AppendableSerialContext context) throws IOException
    {

    }

    @Override
    public void startLinks(MithraObject reladomoObject, AppendableSerialContext appendableSerialContext, int size) throws IOException
    {

    }

    @Override
    public void endLinks(MithraObject reladomoObject, AppendableSerialContext appendableSerialContext) throws IOException
    {

    }

    @Override
    public void startAnnotatedMethod(MithraObject reladomoObject, AppendableSerialContext context, int size) throws IOException
    {

    }

    @Override
    public void endAnnotatedMethod(MithraObject reladomoObject, AppendableSerialContext context) throws IOException
    {

    }

    @Override
    public void startReladomoList(MithraList reladomoList, AppendableSerialContext context) throws IOException
    {
        context.startObject();
        context.getAppendable().append('{');
    }

    @Override
    public void endReladomoList(MithraList reladomoList, AppendableSerialContext context) throws IOException
    {
        context.getAppendable().append('}');
        context.endObject();
    }

    @Override
    public void startReladomoListMetatdata(MithraList reladomoList, AppendableSerialContext context) throws IOException
    {
        startField(context, "_rdoMetaData");
        context.startObject();
        context.getAppendable().append('{');
    }

    @Override
    public void endReladomoListMedatadata(MithraList reladomoList, AppendableSerialContext context) throws IOException
    {
        context.getAppendable().append('}');
        context.endObject();
    }

    @Override
    public void startReladomoListItem(MithraList reladomoList, AppendableSerialContext context, int index, MithraObject reladomoObject) throws IOException
    {
        if (index > 0)
        {
            context.getAppendable().append(",");
        }
    }

    @Override
    public void endReladomoListItem(MithraList reladomoList, AppendableSerialContext appendableSerialContext, int index, MithraObject reladomoObject) throws IOException
    {

    }

    @Override
    public void startReladomoListElements(MithraList reladomoList, AppendableSerialContext context) throws IOException
    {
        startField(context, "elements");
        context.getAppendable().append('[');
    }

    @Override
    public void endReladomoListElements(MithraList reladomoList, AppendableSerialContext context) throws IOException
    {
        context.getAppendable().append(']');
    }
}
