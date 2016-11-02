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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.fw.common.mithra.util.serializer.ReladomoSerializationContext;
import com.gs.fw.common.mithra.util.serializer.SerialWriter;
import com.gs.fw.common.mithra.util.serializer.SerializationConfig;

import java.util.List;

public class GsonRelodomoSerialContext extends ReladomoSerializationContext
{
    private List<JsonElement> result = FastList.newList();

    public GsonRelodomoSerialContext(SerializationConfig serializationConfig, SerialWriter writer, JsonObject result)
    {
        super(serializationConfig, writer);
        this.result.add(result);
    }

    public JsonObject getCurrentResultAsObject()
    {
        return (JsonObject) result.get(result.size() - 1);
    }

    public JsonObject pushNewObject(String name)
    {
        JsonObject newObj = new JsonObject();
        getCurrentResultAsObject().add(name, newObj);
        result.add(newObj);
        return newObj;
    }

    public JsonElement pop()
    {
        return result.remove(result.size() - 1);
    }

    public JsonArray pushNewArray(String name)
    {
        JsonArray newObj = new JsonArray();
        getCurrentResultAsObject().add(name, newObj);
        result.add(newObj);
        return newObj;
    }
}
