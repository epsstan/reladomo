package com.gs.fw.common.mithra.generator.annotationprocessor.annotations;

import com.gs.fw.common.mithra.generator.metamodel.TimestampPrecisionType;

public enum TimestampPrecision
{
    nanonsecond,
    millisecond;

    public TimestampPrecisionType toType()
    {
        if (this == TimestampPrecision.nanonsecond)
        {
            return TimestampPrecisionType.NANOSECOND_PRECISION;
        }
        return TimestampPrecisionType.MILLISECOND_PRECISION;
    }
}
