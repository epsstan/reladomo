package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceResource
{
    Class name();
    boolean readOnlyInterfaces() default false;
}