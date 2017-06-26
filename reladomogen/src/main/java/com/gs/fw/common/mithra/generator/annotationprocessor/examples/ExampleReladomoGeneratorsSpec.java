package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorsSpec;

@ReladomoGeneratorsSpec(
        generators = {
                @ReladomoGeneratorSpec(
                        domain = CustomerDomainListSpec.class,
                        nonGeneratedDir =  "/tmp/foobar",
                        generateConcreteClasses =  false,
                        generateGscListMethod =  true
                )
        }
)
public interface ExampleReladomoGeneratorsSpec
{
}
