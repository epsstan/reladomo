package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;

import java.sql.Timestamp;

@ReladomoObjectSpec(
    packageName = "com.examples.reladomogen",
    defaultTableName = "CUSTOMER"
)
public interface CustomerSpec
{
    @AsOfAttributeSpec(fromColumnName="FROM_Z", toColumnName="THRU_Z", toIsInclusive = false,
        isProcessingDate = false, futureExpiringRowsExist = true,
        infinityDate="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.ExampleInfinityTimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.getInfinityDate()]"
    )
    Timestamp businessDate();

    @AsOfAttributeSpec(fromColumnName="IN_Z", toColumnName="OUT_Z", toIsInclusive = false,
        isProcessingDate = true,
        infinityDate="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.ExampleInfinityTimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.getInfinityDate()]"
    )
    Timestamp processingDate();

    @IntAttributeSpec(columnName = "CUSTOMER_ID")
    int customerId();

    @StringAttributeSpec(columnName = "FIRST_NAME", maxLength = 200, nullable = true)
    String firstName();

    @StringAttributeSpec(columnName = "LAST_NAME", maxLength = 200, nullable = true)
    String lastName();

    @StringAttributeSpec(columnName = "COUNTRY", maxLength = 200, nullable = true)
    String country();

    @RelationshipSpec(
        cardinality = RelationshipSpec.Cardinality.OneToMany,
        contract = "this.customerId == CustomerAccount.customerId",
        relatedIsDependent =  false,
        reverseRelationshipName = "customer"

    )
    CustomerAccountSpec accounts();
}
