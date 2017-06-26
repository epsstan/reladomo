package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import com.gs.fw.common.mithra.generator.metamodel.TimezoneConversionType;

public enum TimezoneConversion
{
    none;

    public TimezoneConversionType toType()
    {
        if (this == none)
        {
            return TimezoneConversionType.NONE;
        }
        throw new UnsupportedOperationException();
    }
}
