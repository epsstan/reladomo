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

package com.gs.fw.common.mithra.util.serializer;

import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import com.gs.fw.common.mithra.MithraList;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.finder.DeepRelationshipAttribute;
import com.gs.fw.common.mithra.finder.RelatedFinder;
import com.gs.fw.common.mithra.util.ListFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class SerializationConfig
{
    private static final UnifiedMap<String, SerializationConfig> BY_NAME = new UnifiedMap<String, SerializationConfig>();

    private SerializationNode rootNode;
    private String annotatedContextName;
    private Set<Method> excludedMethods;

    public static SerializationConfig byName(String name)
    {
        synchronized (BY_NAME)
        {
            return BY_NAME.get(name);
        }
    }

    public void saveOrOverwriteWithName(String name)
    {
        synchronized (BY_NAME)
        {
            BY_NAME.put(name, this);
        }
    }

    public static SerializationConfig shallowWithDefaultAttributes(RelatedFinder finder)
    {
        SerializationConfig config = new SerializationConfig();
        config.rootNode = SerializationNode.withDefaultAttributes(finder);
        return config;
    }

    public static SerializationConfig withDeepFetchesFromList(RelatedFinder finder, MithraList list)
    {
        SerializationConfig config = new SerializationConfig();
        config.rootNode = SerializationNode.withDefaultAttributesAndDeepFetchesFromList(finder, list);
        return config;
    }

    public SerializationNode getRootNode()
    {
        return rootNode;
    }

    public SerializationConfig withoutTheseAttributes(Attribute... attributes)
    {
        SerializationConfig config = new SerializationConfig();
        config.rootNode = this.rootNode.withoutTheseAttributes(attributes);
        config.annotatedContextName = this.annotatedContextName;
        config.excludedMethods = this.excludedMethods;
        return config;
    }

    public SerializationConfig withDeepFetches(DeepRelationshipAttribute... deepFetches)
    {
        SerializationConfig config = new SerializationConfig();
        config.rootNode = this.rootNode.withDeepFetches(deepFetches);
        config.annotatedContextName = this.annotatedContextName;
        config.excludedMethods = this.excludedMethods;
        return config;
    }

    public SerializationConfig withAnnotatedMethods(String contextName)
    {
        SerializationConfig config = new SerializationConfig();
        config.rootNode = this.rootNode;
        config.annotatedContextName = contextName;
        config.excludedMethods = this.excludedMethods;
        return config;
    }

    public SerializationConfig withLinks()
    {
        SerializationConfig config = new SerializationConfig();
        config.rootNode = this.rootNode.withLinks();
        config.annotatedContextName = this.annotatedContextName;
        config.excludedMethods = this.excludedMethods;
        return config;
    }

    public SerializationConfig withoutTheseAnnotatedMethods(Method... methods)
    {
        SerializationConfig config = new SerializationConfig();
        config.rootNode = this.rootNode;
        config.annotatedContextName = this.annotatedContextName;
        UnifiedSet<Method> newMethods = UnifiedSet.newSetWith(methods);
        if (this.excludedMethods != null)
        {
            newMethods.addAll(this.excludedMethods);
        }
        config.excludedMethods = newMethods;
        return config;
    }

    public List<Method> getAnnotatedMethods(Class clazz)
    {
        if (annotatedContextName == null)
        {
            return ListFactory.EMPTY_LIST;
        }
        List<Method> methods = AnnotatedMethodCache.getInstance().get(clazz, this.annotatedContextName);
        if (excludedMethods != null)
        {
            List<Method> filtered = FastList.newList(methods);
            filtered.removeAll(excludedMethods);
            methods = filtered;
        }
        return methods;
    }

}
