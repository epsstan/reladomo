package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import com.gs.fw.common.mithra.generator.metamodel.TimestampPrecisionType;
import com.gs.fw.common.mithra.generator.metamodel.TimezoneConversionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//todo : do all annotations needs to be retained at run time ?
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsOfAttribute
{
    String fromColumnName();

    String toColumnName();

    boolean setAsString() default false;

    boolean toIsInclusive() default true;

    String infinityDate();

    boolean isProcessingDate() default false;

    boolean futureExpiringRowsExist() default false;

    String defaultIfNotSpecified();

    boolean finalGetters() default false;

    boolean infinityIsNull() default false;

    TimestampPrecisionType.Enums timestampPrecision() default TimestampPrecisionType.Enums.nanonsecond;

    TimezoneConversionType.Enums timezoneConversion() default TimezoneConversionType.Enums.none;

    boolean poolable() default true;

    Property[] properties() default {};
}
