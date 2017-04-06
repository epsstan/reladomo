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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gs.fw.common.mithra.attribute.TimestampAttribute;
import com.gs.fw.common.mithra.util.MithraRuntimeCacheController;
import com.gs.fw.common.mithra.util.serializer.ReladomoDeserializer;
import com.gs.fw.common.mithra.util.serializer.ReladomoSerializationContext;
import com.gs.fw.common.mithra.util.serializer.Serialized;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class JacksonReladomoWrappedDeserializer extends StdDeserializer<Serialized<?>> implements ContextualDeserializer
{
    private JavaType valueType;

    public JacksonReladomoWrappedDeserializer()
    {
        super(Serialized.class);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException
    {
        JavaType wrapperType = null;
        if (property != null)
        {
            wrapperType = property.getType();
        }
        else
        {
            wrapperType = ctxt.getContextualType().getContentType();
        }
        if (wrapperType == null)
        {
            return this;
        }
        JavaType valueType = wrapperType.containedType(0);
        JacksonReladomoWrappedDeserializer deserializer = new JacksonReladomoWrappedDeserializer();
        deserializer.valueType = valueType;
        return deserializer;
    }

    @Override
    public Serialized<?> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException
    {
        ReladomoDeserializer deserializer;
        if (this.valueType == null)
        {
            deserializer = new ReladomoDeserializer();

        }
        else
        {
            Class<?> rawClass = this.valueType.getRawClass();
            deserializer = createDeserializer(rawClass);
        }
        boolean nextIsClassName = false;
        boolean nextIsObjectState = false;
        int ignoreDepth = 0;
        do
        {
            JsonToken jsonToken = parser.getCurrentToken();

            if (JsonToken.START_OBJECT.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.startObject();
                }
                else
                {
                    ignoreDepth++;
                }
            }
            else if (JsonToken.END_OBJECT.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.endObject();
                }
                else
                {
                    ignoreDepth--;
                }
            }
            else if (JsonToken.START_ARRAY.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.startListElements();
                }
                else
                {
                    ignoreDepth++;
                }
            }
            else if (JsonToken.END_ARRAY.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.endListElements();
                }
                else
                {
                    ignoreDepth -= 2;
                }
            }
            else if (JsonToken.FIELD_NAME.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {

                    String fieldName = parser.getCurrentName();
                    if (fieldName.equals(ReladomoSerializationContext.RELADOMO_CLASS_NAME))
                    {
                        nextIsClassName = true;
                    }
                    else if (fieldName.equals(ReladomoSerializationContext.RELADOMO_STATE))
                    {
                        nextIsObjectState = true;
                    }
                    else
                    {
                        ReladomoDeserializer.FieldOrRelation fieldOrRelation = deserializer.startFieldOrRelationship(fieldName);
                        if (ReladomoDeserializer.FieldOrRelation.Unknown.equals(fieldOrRelation))
                        {
                            ignoreDepth++;
                        }
                    }
                }
                else
                {
                    ignoreDepth++;
                }
            }
            else if (JsonToken.VALUE_EMBEDDED_OBJECT.equals(jsonToken))
            {
                throw new RuntimeException("Should not be present in the stream");
            }
            else if (JsonToken.VALUE_FALSE.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.setBooleanField(false);
                }
                else
                {
                    ignoreDepth--;
                }
            }
            else if (JsonToken.VALUE_TRUE.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.setBooleanField(true);
                }
                else
                {
                    ignoreDepth--;
                }
            }
            else if (JsonToken.VALUE_NULL.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.setFieldOrRelationshipNull();
                }
                else
                {
                    ignoreDepth--;
                }
            }
            else if (JsonToken.VALUE_STRING.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    if (nextIsClassName)
                    {
                        deserializer.storeReladomoClassName(parser.getValueAsString());
                        nextIsClassName = false;
                    }
                    else
                    {
                        deserializer.parseFieldFromString(parser.getValueAsString());
                    }
                }
                else
                {
                    ignoreDepth--;
                }
            }
            else if (JsonToken.VALUE_NUMBER_INT.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    if (nextIsObjectState)
                    {
                        deserializer.setReladomoObjectState(parser.getValueAsInt());
                        nextIsObjectState = false;
                    }
                    else
                    {
                        if (deserializer.getCurrentAttribute() instanceof TimestampAttribute)
                        {
                            Date date = this._parseDate(parser.getValueAsString(), ctxt);
                            deserializer.setTimestampField(new Timestamp(date.getTime()));
                        }
                        else
                        {
                            deserializer.parseFieldFromString(parser.getValueAsString());
                        }
                    }
                }
                else
                {
                    ignoreDepth--;
                }
            }
            else if (JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken))
            {
                if (ignoreDepth == 0)
                {
                    deserializer.parseFieldFromString(parser.getValueAsString());
                }
                else
                {
                    ignoreDepth--;
                }
            }
            parser.nextToken();
        } while (!parser.isClosed());
        return deserializer.getDeserializedResult();
    }

    private ReladomoDeserializer createDeserializer(Class<?> rawClass) throws IOException
    {
        try
        {
            return new ReladomoDeserializer(new MithraRuntimeCacheController(Class.forName(rawClass.getName() + "Finder")).getFinderInstance());
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Could not finder for class "+rawClass.getName());
        }
    }
}
