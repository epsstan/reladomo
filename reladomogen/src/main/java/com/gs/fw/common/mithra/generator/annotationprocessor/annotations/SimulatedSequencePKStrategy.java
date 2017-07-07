package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimulatedSequencePKStrategy
{
    String sequenceName();

    String sequenceObjectFactoryName();

    boolean hasSourceAttribute() default false;

    int batchSize() default 10;

    int intialValue() default 1;

    int incrementSize() default 1;
}
