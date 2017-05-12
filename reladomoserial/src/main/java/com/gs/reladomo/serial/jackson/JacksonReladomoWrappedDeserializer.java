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
import com.gs.fw.common.mithra.util.serializer.ReladomoDeserialize;
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

        deserializer.setIgnoreUnknown();

        ParserState state = NormalParserState.INSTANCE;
        do
        {
            JsonToken jsonToken = parser.getCurrentToken();

            if (JsonToken.START_OBJECT.equals(jsonToken))
            {
                state = state.startObject(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.END_OBJECT.equals(jsonToken))
            {
                state = state.endObject(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.START_ARRAY.equals(jsonToken))
            {
                state = state.startArray(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.END_ARRAY.equals(jsonToken))
            {
                state = state.endArray(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.FIELD_NAME.equals(jsonToken))
            {
                state = state.fieldName(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.VALUE_EMBEDDED_OBJECT.equals(jsonToken))
            {
                state = state.valueEmbeddedObject(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.VALUE_FALSE.equals(jsonToken))
            {
                state = state.valueFalse(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.VALUE_TRUE.equals(jsonToken))
            {
                state = state.valueTrue(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.VALUE_NULL.equals(jsonToken))
            {
                state = state.valueNull(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.VALUE_STRING.equals(jsonToken))
            {
                state = state.valueString(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.VALUE_NUMBER_INT.equals(jsonToken))
            {
                state = state.valueNumberInt(this, parser, ctxt, jsonToken, deserializer);
            }
            else if (JsonToken.VALUE_NUMBER_FLOAT.equals(jsonToken))
            {
                state = state.valueNumberFloat(this, parser, ctxt, jsonToken, deserializer);
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

    private abstract static class ParserState
    {
        ParserState startObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call startObject in "+this.getClass().getSimpleName());
        }
        ParserState endObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call endObject in "+this.getClass().getSimpleName());
        }
        ParserState startArray(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call startArray in "+this.getClass().getSimpleName());
        }
        ParserState endArray(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call endArray in "+this.getClass().getSimpleName());
        }
        ParserState fieldName(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call fieldName in "+this.getClass().getSimpleName());
        }
        ParserState valueEmbeddedObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call valueEmbeddedObject in "+this.getClass().getSimpleName());
        }
        ParserState valueTrue(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call valueTrue in "+this.getClass().getSimpleName());
        }
        ParserState valueFalse(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call valueFalse in "+this.getClass().getSimpleName());
        }
        ParserState valueNull(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call valueNull in "+this.getClass().getSimpleName());
        }
        ParserState valueString(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call valueString in "+this.getClass().getSimpleName());
        }
        ParserState valueNumberInt(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call valueNumberInt in "+this.getClass().getSimpleName());
        }
        ParserState valueNumberFloat(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            throw new RuntimeException("Shouldn't call valueNumberFloat in "+this.getClass().getSimpleName());
        }
    }

    private static class NormalParserState extends ParserState
    {
        public static NormalParserState INSTANCE = new NormalParserState();
        @Override
        ParserState startObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.startObject();
            return this;
        }

        @Override
        ParserState endObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.endObjectOrList();
            return this;
        }

        @Override
        ParserState fieldName(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            String fieldName = parser.getCurrentName();
            if (fieldName.equals(ReladomoSerializationContext.RELADOMO_CLASS_NAME))
            {
                return ClassNameState.INSTANCE;
            }
            else if (fieldName.equals(ReladomoSerializationContext.RELADOMO_STATE))
            {
                return ObjectStateState.INSTANCE;
            }
            else
            {
                ReladomoDeserializer.FieldOrRelation fieldOrRelation = deserializer.startFieldOrRelationship(fieldName);
                if (ReladomoDeserializer.FieldOrRelation.Unknown.equals(fieldOrRelation))
                {
                    return this;
                }
                else if (ReladomoDeserializer.FieldOrRelation.ToManyRelationship.equals(fieldOrRelation))
                {
                    return new ToManyState(this);
                }
            }
            
            return this;
        }

        @Override
        ParserState endArray(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.endListElements();
            return this;
        }

        @Override
        ParserState valueTrue(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.setBooleanField(true);
            return this;
        }

        @Override
        ParserState valueFalse(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.setBooleanField(false);
            return this;
        }

        @Override
        ParserState valueNull(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.setFieldOrRelationshipNull();
            return this;
        }

        @Override
        ParserState valueString(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.parseFieldFromString(parser.getValueAsString());
            return this;
        }

        @Override
        ParserState valueNumberInt(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            if (deserializer.getCurrentAttribute() instanceof TimestampAttribute)
            {
                Date date = jacksonDeserializer._parseDate(parser.getValueAsString(), ctxt);
                deserializer.setTimestampField(new Timestamp(date.getTime()));
            }
            else
            {
                deserializer.parseFieldFromString(parser.getValueAsString());
            }
            return this;
        }

        @Override
        ParserState valueNumberFloat(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.parseFieldFromString(parser.getValueAsString());
            return this;
        }
    }
    
    private static class ClassNameState extends ParserState
    {
        public static ClassNameState INSTANCE = new ClassNameState();
        @Override
        ParserState valueString(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.storeReladomoClassName(parser.getValueAsString());
            return NormalParserState.INSTANCE;
        }
    }
    
    private static class ObjectStateState extends ParserState
    {
        public static ObjectStateState INSTANCE = new ObjectStateState();

        @Override
        ParserState valueNumberInt(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.setReladomoObjectState(parser.getValueAsInt());
            return NormalParserState.INSTANCE;
        }
    }


    private static class IgnoreState extends ParserState
    {
        protected ParserState previous;

        public IgnoreState(ParserState previous)
        {
            this.previous = previous;
        }

        @Override
        ParserState startObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return new IgnoreState(this);
        }

        @Override
        ParserState endObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return previous;
        }

        @Override
        ParserState startArray(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState endArray(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState fieldName(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState valueEmbeddedObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState valueTrue(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState valueFalse(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState valueNull(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState valueString(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState valueNumberInt(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }

        @Override
        ParserState valueNumberFloat(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            return this;
        }
    }

    private static class ToManyState extends IgnoreState
    {

        public ToManyState(ParserState previous)
        {
            super(previous);
        }

        @Override
        ParserState startObject(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.startList();
            return new WaitForElementsState(previous);
        }
    }

    private static class WaitForElementsState extends IgnoreState
    {
        public WaitForElementsState(ParserState previous)
        {
            super(previous);
        }

        @Override
        ParserState fieldName(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            String fieldName = parser.getCurrentName();
            if ("elements".equals(fieldName))
            {
                return new ToManyList(previous);
            }
            return this;
        }
    }

    private static class ToManyList extends ParserState
    {
        private ParserState previous;

        public ToManyList(ParserState previous)
        {
            this.previous = previous;
        }

        @Override
        ParserState startArray(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.startListElements();
            return new InArrayState(previous);
        }
    }

    private static class InArrayState extends NormalParserState
    {
        private ParserState previous;

        public InArrayState(ParserState previous)
        {
            this.previous = previous;
        }

        @Override
        ParserState endArray(JacksonReladomoWrappedDeserializer jacksonDeserializer, JsonParser parser, DeserializationContext ctxt, JsonToken jsonToken, ReladomoDeserializer deserializer) throws IOException
        {
            deserializer.endListElements();
            return previous;
        }
    }
}
