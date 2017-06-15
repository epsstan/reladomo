package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntAttributeSpec
{

// Inferred
//    name=" xsd:token [1] ?"
//    javaType=" xsd:token [1] ?"

    boolean useForOptimisticLocking() default true;

    String columnName();

    boolean nullable() default true;
}
