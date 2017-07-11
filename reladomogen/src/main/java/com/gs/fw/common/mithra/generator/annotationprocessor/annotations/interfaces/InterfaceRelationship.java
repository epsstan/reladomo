package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces;

import com.gs.fw.common.mithra.generator.metamodel.CardinalityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceRelationship
{
    //name derived from class name
    //related object derived from method return type
    // this is the relationship expression
    CardinalityType.Enums cardinality();
    String parameters() default "";
}
