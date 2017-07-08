package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectResource
{
    Class name();

    boolean replicated() default false;

    boolean generateInterfaces() default false;

    boolean enableOffHeap() default false;

    boolean  readOnlyInterfaces() default false;

}
