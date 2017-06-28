package com.test1.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;

@ReladomoListSpec(
        resources = {
                @ObjectResource(name = CustomerSpec.class, replicated = false, generateInterfaces = false, enableOffHeap = false),
                @ObjectResource(name = CustomerAccountSpec.class, replicated = false, generateInterfaces = false, enableOffHeap = false)
        }
)
public interface CustomerDomainListSpec
{
}
