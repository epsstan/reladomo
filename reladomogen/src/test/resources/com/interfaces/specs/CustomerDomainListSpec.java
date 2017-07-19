package com.interfaces.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.*;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.*;

@ReladomoListSpec(
        resources = {
                @ObjectResource(name = CustomerSpec.class,  generateInterfaces = false, enableOffHeap = false),
                @ObjectResource(name = CustomerAccountSpec.class, replicated = false, enableOffHeap = false)
        },
        interfaces = {
                @InterfaceResource(name = CustomerIdInterfaceSpec.class, readOnlyInterfaces = false),
                @InterfaceResource(name = CustomerId2InterfaceSpec.class, readOnlyInterfaces = false)
        }
)
public class CustomerDomainListSpec
{
}
