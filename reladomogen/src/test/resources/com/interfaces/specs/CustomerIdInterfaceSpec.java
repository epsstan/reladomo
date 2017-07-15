package com.interfaces.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.InterfaceAttribute;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.InterfaceRelationship;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.ReladomoInterface;
import com.gs.fw.common.mithra.generator.metamodel.CardinalityType;
import java.math.BigDecimal;

@ReladomoInterface(
        packageName = "com.interfaces.domain"
)
public interface CustomerIdInterfaceSpec
{
    @InterfaceAttribute
    int customerId();

    @InterfaceRelationship(cardinality = CardinalityType.Enums.ONE_TO_MANY)
    CustomerAccountSpec accounts();
}
