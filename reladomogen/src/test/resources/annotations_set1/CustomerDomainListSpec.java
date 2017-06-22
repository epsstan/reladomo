package annotations_set1;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ObjectResourceSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoListSpec;

@ReladomoListSpec(
        resources = {
                @ObjectResourceSpec(name = CustomerSpec.class, replicated = false, generateInterfaces = false, enableOffHeap = false),
                @ObjectResourceSpec(name = CustomerAccountSpec.class, replicated = false, generateInterfaces = false, enableOffHeap = false)
        }
)
public interface CustomerDomainListSpec
{
}
