package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ObjectResource;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoListSpec;

@ReladomoListSpec(
        resources = {
                @ObjectResource(name = CustomerSpec.class, replicated = false, generateInterfaces = false, enableOffHeap = false),
                @ObjectResource(name = CustomerAccountSpec.class, replicated = false, generateInterfaces = false, enableOffHeap = false)
        }
)
public interface CustomerDomainListSpec
{
}
