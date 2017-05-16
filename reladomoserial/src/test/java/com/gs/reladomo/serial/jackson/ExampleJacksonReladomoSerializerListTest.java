package com.gs.reladomo.serial.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gs.fw.common.mithra.test.domain.Order;
import com.gs.fw.common.mithra.test.domain.OrderFinder;
import com.gs.fw.common.mithra.test.domain.OrderList;
import com.gs.fw.common.mithra.test.domain.SerialView;
import com.gs.fw.common.mithra.test.util.serializer.TestTrivialJsonList;
import com.gs.fw.common.mithra.util.serializer.ReladomoSerializationContext;
import com.gs.fw.common.mithra.util.serializer.SerializationConfig;
import com.gs.fw.common.mithra.util.serializer.Serialized;
import com.gs.fw.common.mithra.util.serializer.SerializedList;
import org.junit.Test;

/**
 * Created by me on 5/12/2017.
 */
public class ExampleJacksonReladomoSerializerListTest extends TestTrivialJsonList
{

    protected SerializedList fromJson(String json) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonReladomoModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JavaType customClassCollection = mapper.getTypeFactory().constructMapLikeType(SerializedList.class, Order.class, OrderList.class);

        return mapper.readValue(json, customClassCollection);
//        return mapper.readValue(json, new TypeReference<Serialized<Order>>() {});
//        return mapper.readValue(json, Serialized.class);
    }

    @Override
    protected String toJson(SerializedList serialized) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonReladomoModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper.writeValueAsString(serialized);
    }

    @Override
    @Test
    public void testOrder() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        String sb = toJson(new SerializedList<Order, OrderList>((OrderFinder.findMany(OrderFinder.all())), config));

        SerializedList<Order, OrderList> serialized = fromJson(sb);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
//        assertTrue(serialized.getWrapped().zIsDetached());

    }

    @Override
    @Test
    public void testOrderWithDependentsAndLongMethods() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepDependents();
        config = config.withAnnotatedMethods(SerialView.Longer.class);

        String sb = toJson(new SerializedList<Order, OrderList>((OrderFinder.findMany(OrderFinder.all())), config));

        SerializedList<Order, OrderList> serialized = fromJson(sb);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
    }

    @Test
    public void testDeletedOrder() throws Exception
    {
        String json = "{\n" +
                "  \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "  \"_rdoListSize\" : 7,\n" +
                "  \"elements\" : [ {\n" +
                "    \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "    \"_rdoState\" : "+ ReladomoSerializationContext.DELETED_OR_TERMINATED_STATE+",\n" +
                "    \"orderId\" : 1,\n" +
                "    \"orderDate\" : 1073883600000,\n" +
                "    \"userId\" : 1,\n" +
                "    \"description\" : \"First order\",\n" +
                "    \"state\" : \"In-Progress\",\n" +
                "    \"trackingId\" : \"123\"\n" +
                "  }, {\n" +
                "    \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "    \"_rdoState\" : 20,\n" +
                "    \"orderId\" : 2,\n" +
                "    \"orderDate\" : 1076562000000,\n" +
                "    \"userId\" : 1,\n" +
                "    \"description\" : \"Second order\",\n" +
                "    \"state\" : \"In-Progress\",\n" +
                "    \"trackingId\" : \"124\"\n" +
                "  }, {\n" +
                "    \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "    \"_rdoState\" : 20,\n" +
                "    \"orderId\" : 3,\n" +
                "    \"orderDate\" : 1079067600000,\n" +
                "    \"userId\" : 1,\n" +
                "    \"description\" : \"Third order\",\n" +
                "    \"state\" : \"In-Progress\",\n" +
                "    \"trackingId\" : \"125\"\n" +
                "  }, {\n" +
                "    \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "    \"_rdoState\" : 20,\n" +
                "    \"orderId\" : 4,\n" +
                "    \"orderDate\" : 1081742400000,\n" +
                "    \"userId\" : 2,\n" +
                "    \"description\" : \"Fourth order, different user\",\n" +
                "    \"state\" : \"In-Progress\",\n" +
                "    \"trackingId\" : \"126\"\n" +
                "  }, {\n" +
                "    \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "    \"_rdoState\" : 20,\n" +
                "    \"orderId\" : 55,\n" +
                "    \"orderDate\" : 1081742400000,\n" +
                "    \"userId\" : 3,\n" +
                "    \"description\" : \"Order number five\",\n" +
                "    \"state\" : \"Gummed up\",\n" +
                "    \"trackingId\" : \"127\"\n" +
                "  }, {\n" +
                "    \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "    \"_rdoState\" : 20,\n" +
                "    \"orderId\" : 56,\n" +
                "    \"orderDate\" : 1081742400000,\n" +
                "    \"userId\" : 3,\n" +
                "    \"description\" : \"Sixth order, different user\",\n" +
                "    \"state\" : \"Gummed up\",\n" +
                "    \"trackingId\" : \"128\"\n" +
                "  }, {\n" +
                "    \"_rdoClassName\" : \"com.gs.fw.common.mithra.test.domain.Order\",\n" +
                "    \"_rdoState\" : 20,\n" +
                "    \"orderId\" : 57,\n" +
                "    \"orderDate\" : 1081742400000,\n" +
                "    \"userId\" : 3,\n" +
                "    \"description\" : \"Seventh order, different user\",\n" +
                "    \"state\" : \"Gummed up\",\n" +
                "    \"trackingId\" : \"129\"\n" +
                "  } ]\n" +
                "}";

        SerializedList<Order, OrderList> serialized = fromJson(json);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
        list.copyDetachedValuesToOriginalOrInsertIfNewOrDeleteIfRemoved();
        assertNull(OrderFinder.findByPrimaryKey(1));
    }

    @Override
    public void testOrderWithItems() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepFetches(OrderFinder.items());
        String sb = toJson(new SerializedList<Order, OrderList>((OrderFinder.findMany(OrderFinder.all())), config));

        SerializedList<Order, OrderList> serialized = fromJson(sb);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
    }

    @Override
    public void testOrderWithItemsAndStatus() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepFetches(OrderFinder.orderStatus(), OrderFinder.items());
        config = config.withAnnotatedMethods(SerialView.Shorter.class);

        String sb = toJson(new SerializedList<Order, OrderList>((OrderFinder.findMany(OrderFinder.all())), config));

        SerializedList<Order, OrderList> serialized = fromJson(sb);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
    }

    @Override
    public void testOrderTwoDeep() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepFetches(OrderFinder.orderStatus(), OrderFinder.items().orderItemStatus());
        String sb = toJson(new SerializedList<Order, OrderList>((OrderFinder.findMany(OrderFinder.all())), config));

        SerializedList<Order, OrderList> serialized = fromJson(sb);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
    }

    @Override
    public void testOrderWithDependents() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepDependents();
        String sb = toJson(new SerializedList<Order, OrderList>((OrderFinder.findMany(OrderFinder.all())), config));

        SerializedList<Order, OrderList> serialized = fromJson(sb);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
    }

    @Override
    public void testOrderWithDependentsNoMeta() throws Exception
    {
        SerializationConfig config = SerializationConfig.shallowWithDefaultAttributes(OrderFinder.getFinderInstance());
        config = config.withDeepDependents();
        config = config.withoutMetaData();
        String sb = toJson(new SerializedList<Order, OrderList>((OrderFinder.findMany(OrderFinder.all())), config));

        SerializedList<Order, OrderList> serialized = fromJson(sb);
        OrderList list = serialized.getWrapped();
        assertEquals(7, list.size());
        for(int i=0;i<list.size();i++)
        {
            assertTrue(list.get(i).zIsDetached());
        }
    }
}
