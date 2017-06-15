package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//todo : do all annotations needs to be retained at run time ?
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectResourceSpec
{
    boolean replicated() default false;

    boolean generateInterfaces() default false;

    boolean enableOffHeap() default false;
}
