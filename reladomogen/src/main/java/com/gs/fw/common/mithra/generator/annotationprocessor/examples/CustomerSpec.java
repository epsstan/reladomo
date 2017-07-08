package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.metamodel.CardinalityType;
import com.gs.fw.common.mithra.generator.metamodel.ObjectType;

import java.sql.Timestamp;

@com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ReladomoObject(
    packageName = "com.examples.reladomogen",
    defaultTableName = "CUSTOMER",
    objectType = ObjectType.Enums.TRANSACTIONAL
)
public interface CustomerSpec
{
    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.AsOfAttribute(fromColumnName="FROM_Z", toColumnName="THRU_Z", toIsInclusive = false,
        isProcessingDate = false, futureExpiringRowsExist = true,
        infinityDate="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.ExampleInfinityTimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.getInfinityDate()]"
    )
    Timestamp businessDate();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.AsOfAttribute(fromColumnName="IN_Z", toColumnName="OUT_Z", toIsInclusive = false,
        isProcessingDate = true,
        infinityDate="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.ExampleInfinityTimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.getInfinityDate()]"
    )
    Timestamp processingDate();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.PrimaryKey()
    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.MaxPKStrategy()
    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.IntAttribute(columnName = "CUSTOMER_ID")
    int customerId();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.StringAttribute(columnName = "FIRST_NAME", maxLength = 200, nullable = true)
    String firstName();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.StringAttribute(columnName = "LAST_NAME", maxLength = 200, nullable = true)
    String lastName();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.StringAttribute(columnName = "COUNTRY", maxLength = 200, nullable = true)
    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.Properties({
            @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.Property(key = "a1", value = "b1"),
            @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.Property(key = "a2", value = "b2"),
            @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.Property(key = "a3", value = "b3"),
    })
    String country();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.Relationship(
        cardinality = CardinalityType.Enums.ONE_TO_MANY,
        contract = "this.customerId = CustomerAccount.customerId",
        relatedIsDependent =  false,
        reverseRelationshipName = "customer"

    )
    CustomerAccountSpec accounts();
}
