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


public class SuperClassType
 extends SuperClassTypeAbstract

{
    public static final SuperClassType TABLE_PER_SUBLCASS = new SuperClassType().with("table-per-subclass", null);
    public static final SuperClassType TABLE_ALL_SUBCLASSES = new SuperClassType().with("table-for-all-subclasses", null);
    public static final SuperClassType TABLE_PER_CLASS = new SuperClassType().with("table-per-class", null);

    public enum Enums
    {
        TABLE_PER_SUBLCASS(SuperClassType.TABLE_PER_SUBLCASS),
        TABLE_ALL_SUBCLASSES(SuperClassType.TABLE_ALL_SUBCLASSES),
        TABLE_PER_CLASS(SuperClassType.TABLE_PER_CLASS);

        private SuperClassType type;

        Enums(SuperClassType type)
        {

            this.type = type;
        }

        public SuperClassType getType()
        {
            return type;
        }
    }
}
