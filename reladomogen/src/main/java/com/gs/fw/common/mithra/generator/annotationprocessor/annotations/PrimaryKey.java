package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey
{
    enum GeneratorStrategy
    {
        Max, SimulatedSequence
    }

    boolean mutable() default false;

    GeneratorStrategy generatorStrategy() default GeneratorStrategy.SimulatedSequence;
}
