package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LongAttribute
{
    //generic attributes

    String columnName();
    boolean nullable() default false;
    String defaultIfNull() default "";
    boolean readonly() default false;
    boolean inPlaceUpdate() default false;
    boolean finalGetter() default false;

    //specific attributes
    boolean useForOptimisticLocking() default false;
}
