package com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces;

import com.gs.fw.common.mithra.generator.metamodel.TimezoneConversionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//todo : do all annotations needs to be retained at run time ?
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceAsOfAttribute
{
    boolean toIsInclusive() default true;
    String infinityDate();
    boolean infinityIsNull() default false;
    boolean isProcessingDate() default false;
    TimezoneConversionType.Enums timezoneConversion() default TimezoneConversionType.Enums.none;
}
