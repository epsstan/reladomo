package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringAttributeSpec
{
    int maxLength() default 100;

    boolean truncate() default false;

    String columnName();

    boolean poolable() default true;

    boolean trim() default true;

    boolean nullable() default false;

    boolean readonly() default false;

    boolean inPlaceUpdate() default false;

    boolean finalGetter() default false;

// Todo
//    primaryKey=" xsd:boolean [0..1] ?"
//    identity=" xsd:boolean [0..1] ?"
//    primaryKeyGeneratorStrategy=" PrimaryKeyGeneratorStrategyType [0..1] ?"
//    defaultIfNull=" xsd:token [0..1] ?"
//    mutablePrimaryKey=" xsd:boolean [0..1] ?"
//    modifyTimePrecisionOnSet=" TimePrecisionType [0..1] ?"
//    precision=" xsd:int [0..1] ?"
//    scale=" xsd:int [0..1] ?"
//    <SimulatedSequence> SimulatedSequenceType </SimulatedSequence> [0..1] ?
//    <Property> PropertyType </Property> [0..*] ?
}
