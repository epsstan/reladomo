package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringAttributeSpec
{

// Inferred
//    name=" xsd:token [1] ?"
//    javaType=" xsd:token [1] ?"

    int maxLength() default 100;

    boolean truncate() default false;

    String columnName();

    boolean poolable() default true;

    boolean trim() default true;

    boolean nullable() default false;

// Todo
//    primaryKey=" xsd:boolean [0..1] ?"
//    identity=" xsd:boolean [0..1] ?"
//    primaryKeyGeneratorStrategy=" PrimaryKeyGeneratorStrategyType [0..1] ?"
//    nullable=" xsd:boolean [0..1] ?"
//    defaultIfNull=" xsd:token [0..1] ?"
//    timezoneConversion=" TimezoneConversionType [0..1] ?"
//    timestampPrecision=" TimestampPrecisionType [0..1] ?"
//    mutablePrimaryKey=" xsd:boolean [0..1] ?"
//    readonly=" xsd:boolean [0..1] ?"
//    modifyTimePrecisionOnSet=" TimePrecisionType [0..1] ?"
//    inPlaceUpdate=" xsd:boolean [0..1] ?"
//    precision=" xsd:int [0..1] ?"
//    scale=" xsd:int [0..1] ?"
//    finalGetter=" xsd:boolean [0..1] ?"
//    useForOptimisticLocking=" xsd:boolean [0..1] ?"
//    <SimulatedSequence> SimulatedSequenceType </SimulatedSequence> [0..1] ?
//    <Property> PropertyType </Property> [0..*] ?
}
