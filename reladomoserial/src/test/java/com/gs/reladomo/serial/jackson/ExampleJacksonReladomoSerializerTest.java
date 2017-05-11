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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gs.fw.common.mithra.test.domain.Order;
import com.gs.fw.common.mithra.test.domain.OrderFinder;
import com.gs.fw.common.mithra.test.domain.OrderItemFinder;
import com.gs.fw.common.mithra.test.util.serializer.TestTrivialJson;
import com.gs.fw.common.mithra.util.serializer.SerializationConfig;
import com.gs.fw.common.mithra.util.serializer.Serialized;
import org.junit.Test;

public class ExampleJacksonReladomoSerializerTest extends TestTrivialJson
{
    @Override
    protected String toJson(Serialized serialized) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonReladomoModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper.writeValueAsString(serialized);
    }

    protected Serialized fromJson(String json) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonReladomoModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JavaType customClassCollection = mapper.getTypeFactory().constructCollectionLikeType(Serialized.class, Order.class);

        return mapper.readValue(json, customClassCollection);
//        return mapper.readValue(json, new TypeReference<Serialized<Order>>() {});
//        return mapper.readValue(json, Serialized.class);
    }

    @Test
    public void testOrder() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(1))), config));

        Serialized<Order> serialized = fromJson(sb);
        assertEquals(1, serialized.getWrapped().getOrderId());
        assertTrue(serialized.getWrapped().zIsDetached());
    }

    @Test
    public void testOrderWithItems() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepFetches(OrderFinder.items());
        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(1))), config));

        Serialized<Order> serialized = fromJson(sb);
        assertEquals(1, serialized.getWrapped().getOrderId());
        assertTrue(serialized.getWrapped().zIsDetached());
        assertEquals(1, serialized.getWrapped().getItems().size());
    }


}
