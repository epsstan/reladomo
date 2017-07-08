package com.test1.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.*;

@ReladomoListSpec(
        resources = {
                @ObjectResource(name = CustomerSpec.class,  generateInterfaces = false, enableOffHeap = false),
                @ObjectResource(name = CustomerAccountSpec.class, replicated = false, enableOffHeap = false)
        }
)
public class CustomerDomainListSpec
{
}
