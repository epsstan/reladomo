package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuperClass
{
    boolean generated();

    String name();
}
