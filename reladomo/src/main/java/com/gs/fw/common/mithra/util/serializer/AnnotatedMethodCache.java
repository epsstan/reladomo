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
import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

public class AnnotatedMethodCache
{
    private static Logger logger = LoggerFactory.getLogger(AnnotatedMethodCache.class.getName());
    private static AnnotatedMethodCache ourInstance = new AnnotatedMethodCache();

    public static AnnotatedMethodCache getInstance()
    {
        return ourInstance;
    }

    private ConcurrentHashMap<ClassAndContextName, List<Method>> cache = ConcurrentHashMap.newMap();

    private AnnotatedMethodCache()
    {
    }

    public List<Method> get(Class clazz, String contextName)
    {
        ClassAndContextName key = new ClassAndContextName(clazz, contextName);
        List<Method> methods = cache.get(key);
        if (methods == null)
        {
            methods = findMethods(clazz, contextName);
            cache.put(key, methods);
        }
        return methods;
    }

    private List<Method> findMethods(Class clazz, String contextName)
    {
        Method[] allMethods = clazz.getMethods();
        FastList<Method> result = FastList.newList();
        for(Method method: allMethods)
        {
            ReladomoSerialize annotation = method.getAnnotation(ReladomoSerialize.class);
            String[] names = annotation.contextNames();
            for(String name: names)
            {
                if (name.equals(contextName))
                {
                    if (method.getParameterTypes().length == 0)
                    {
                        if (method.getReturnType().equals(Void.TYPE))
                        {
                            logger.warn("Incorrect method annotation in class "+clazz.getName()+" method "+method.getName()+" @ReladomoSerialize can only be used with methods that return something");
                        }
                        else
                        {
                            result.add(method);
                        }
                    }
                    else
                    {
                        logger.warn("Incorrect method annotation in class "+clazz.getName()+" method "+method.getName()+" @ReladomoSerialize can only be used with methods that have no parameters");
                    }
                }
            }
        }
        result.trimToSize();
        return result;
    }

    private static class ClassAndContextName
    {
        private final Class clazz;
        private final String contextName;

        public ClassAndContextName(Class clazz, String contextName)
        {
            this.clazz = clazz;
            this.contextName = contextName;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClassAndContextName that = (ClassAndContextName) o;

            if (!clazz.equals(that.clazz)) return false;
            return contextName.equals(that.contextName);

        }

        @Override
        public int hashCode()
        {
            int result = clazz.hashCode();
            result = 31 * result + contextName.hashCode();
            return result;
        }
    }
}
