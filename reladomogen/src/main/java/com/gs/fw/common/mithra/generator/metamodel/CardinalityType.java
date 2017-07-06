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

public class CardinalityType
 extends CardinalityTypeAbstract

{
    public enum Enums
    {
        ONE_TO_MANY(new CardinalityType().with("one-to-many", null)),
        MANY_TO_MANY(new CardinalityType().with("many-to-many", null)),
        ONE_TO_ONE(new CardinalityType().with("one-to-one", null)),
        MANY_TO_ONE(new CardinalityType().with("many-to-one", null));

        private CardinalityType type;

        Enums(CardinalityType type)
        {
            this.type = type;
        }

        public CardinalityType getType()
        {
            return type;
        }
    }
}
