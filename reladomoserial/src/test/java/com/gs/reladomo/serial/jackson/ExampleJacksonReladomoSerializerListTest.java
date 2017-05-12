package com.gs.reladomo.serial.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gs.fw.common.mithra.test.util.serializer.TestTrivialJsonList;
import com.gs.fw.common.mithra.util.serializer.SerializedList;

/**
 * Created by me on 5/12/2017.
 */
public class ExampleJacksonReladomoSerializerListTest extends TestTrivialJsonList
{
    @Override
    protected String toJson(SerializedList serialized) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JacksonReladomoModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper.writeValueAsString(serialized);
    }
}
