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

import com.gs.fw.common.mithra.test.MithraTestAbstract;
import com.gs.fw.common.mithra.test.domain.Order;
import com.gs.fw.common.mithra.test.domain.OrderFinder;
import com.gs.fw.common.mithra.test.domain.SerialView;
import com.gs.fw.common.mithra.util.serializer.SerializationConfig;
import com.gs.fw.common.mithra.util.serializer.Serialized;
import org.junit.Test;

public class TestTrivialJson extends MithraTestAbstract
{
    static
    {
        SerializationConfig thin = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        thin = thin.withoutMetaData();
        thin.saveOrOverwriteWithName("thinOrder");

        SerializationConfig thick = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        thick.withDeepDependents();
        thin.saveOrOverwriteWithName("thickOrder");
    }

    public Serialized<Order> getThinOrder()
    {
        Order order = OrderFinder.findOne(OrderFinder.orderId().eq(1));
        return new Serialized<Order>(order, "thinOrder");
    }

    public Serialized<Order> getThickOrder()
    {
        Order order = OrderFinder.findOne(OrderFinder.orderId().eq(1));
        return new Serialized<Order>(order, "thickOrder");
    }

    @Test
    public void testOrder() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(1))), config));

        System.out.println(sb);
    }

    @Test
    public void testOrderWithItems() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepFetches(OrderFinder.items());
        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(2))), config));

        System.out.println(sb);
    }

    @Test
    public void testOrderWithItemsAndStatus() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepFetches(OrderFinder.orderStatus(), OrderFinder.items());
        config = config.withAnnotatedMethods(SerialView.Shorter.class);

        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(1))), config));

        System.out.println(sb);
    }

    @Test
    public void testOrderTwoDeep() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepFetches(OrderFinder.orderStatus(), OrderFinder.items().orderItemStatus());
        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(1))), config));

        System.out.println(sb);
    }

    @Test
    public void testOrderWithDependents() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepDependents();
        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(2))), config));

        System.out.println(sb);
    }

    @Test
    public void testOrderWithDependentsAndLongMethods() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepDependents();
        config = config.withAnnotatedMethods(SerialView.Longer.class);

        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(2))), config));

        System.out.println(sb);
    }

    @Test
    public void testOrderWithDependentsNoMeta() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepDependents();
        config = config.withoutMetaData();
        String sb = toJson(new Serialized((OrderFinder.findOne(OrderFinder.orderId().eq(1))), config));

        System.out.println(sb);
    }
    
    protected String toJson(Serialized serialized) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        AppendableSerialContext context = new AppendableSerialContext(serialized.getConfig(), new TrivialJsonSerialWriter(), sb);
        context.serializeReladomoObject(serialized.getWrapped());
        return sb.toString();
    }

}
