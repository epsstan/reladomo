package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import com.gs.fw.common.mithra.generator.metamodel.TimezoneConversionType;

public enum TimezoneConversion
{
    none;

    public TimezoneConversionType toType()
    {
        return TimezoneConversionType.NONE;
    }
}
