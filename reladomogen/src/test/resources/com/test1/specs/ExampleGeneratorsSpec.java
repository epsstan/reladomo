package com.test1.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorsSpec;

@ReladomoGeneratorsSpec(
        generators = {
                @ReladomoGeneratorSpec(
                        domain = CustomerDomainListSpec.class,
                        nonGeneratedDir =  "__NON_GENERATED_DIR__",
                        generateConcreteClasses =  false,
                        generateGscListMethod =  true
                )
        }
)
public interface ExampleGeneratorsSpec
{
}
