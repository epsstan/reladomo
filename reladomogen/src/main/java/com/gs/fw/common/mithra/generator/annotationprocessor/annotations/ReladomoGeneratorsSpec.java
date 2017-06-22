package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Grouping of multiple generators.
    This is similar to a sequence of reladomo-gen xml tags.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReladomoGeneratorsSpec
{
    ReladomoGeneratorSpec[] generators();
}
