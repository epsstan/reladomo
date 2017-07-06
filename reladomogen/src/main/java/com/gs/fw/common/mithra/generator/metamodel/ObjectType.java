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

package com.gs.fw.common.mithra.generator.metamodel;

public class ObjectType
 extends ObjectTypeAbstract

{
    public static final ObjectType TRANSACTIONAL = new ObjectType().with("transactional", null);
    public static final ObjectType READ_ONLY = new ObjectType().with("read-only", null);

    public enum Enums
    {
        TRANSACTIONAL(ObjectType.TRANSACTIONAL),
        READ_ONLY(ObjectType.READ_ONLY);

        private ObjectType type;

        Enums(ObjectType type)
        {

            this.type = type;
        }

        public ObjectType getType()
        {
            return type;
        }
    }

}
