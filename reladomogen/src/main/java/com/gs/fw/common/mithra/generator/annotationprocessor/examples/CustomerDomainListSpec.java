package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ObjectResourceSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoListSpec;

@ReladomoListSpec
public interface CustomerDomainListSpec
{
    @ObjectResourceSpec(replicated = false, generateInterfaces = false, enableOffHeap = false)
    CustomerSpec customer();

    @ObjectResourceSpec(replicated = false, generateInterfaces = false, enableOffHeap = false)
    CustomerAccountSpec customerAccount();
}
