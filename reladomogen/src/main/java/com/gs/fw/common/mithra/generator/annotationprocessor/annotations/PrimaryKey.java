package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import com.gs.fw.common.mithra.generator.metamodel.PrimaryKeyGeneratorStrategyType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey
{
    boolean mutable() default false;

    PrimaryKeyGeneratorStrategyType.Enums generatorStrategy() default PrimaryKeyGeneratorStrategyType.Enums.Max;
}
