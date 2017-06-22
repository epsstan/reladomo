package annotations_set1;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoGeneratorsSpec;

@ReladomoGeneratorsSpec(
        generators = {
                @ReladomoGeneratorSpec(
                        domain = CustomerDomainListSpec.class,
                        generatedDir =  "/tmp/foobar",
                        generateConcreteClasses =  false,
                        generateGscListMethod =  true
                )
        }
)
public interface ExampleGeneratorsSpec
{
}
