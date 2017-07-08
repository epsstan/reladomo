package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringAttribute
{
    //generic attributes

    String columnName();
    boolean nullable() default false;
    boolean readonly() default false;
    boolean inPlaceUpdate() default false;
    boolean finalGetter() default false;

    //specific attributes

    int maxLength() default 100;
    boolean truncate() default false;
    boolean trim() default true;
    boolean poolable() default true;
}
