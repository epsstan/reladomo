package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;

import java.sql.Timestamp;

@ReladomoObject(
    packageName = "com.examples.reladomogen",
    defaultTableName = "CUSTOMER"
)
public interface CustomerSpec
{
    @AsOfAttribute(fromColumnName="FROM_Z", toColumnName="THRU_Z", toIsInclusive = false,
        isProcessingDate = false, futureExpiringRowsExist = true,
        infinityDate="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.ExampleInfinityTimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.getInfinityDate()]"
    )
    Timestamp businessDate();

    @AsOfAttribute(fromColumnName="IN_Z", toColumnName="OUT_Z", toIsInclusive = false,
        isProcessingDate = true,
        infinityDate="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.ExampleInfinityTimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.getInfinityDate()]"
    )
    Timestamp processingDate();

    @PrimaryKey(generatorStrategy = PrimaryKey.GeneratorStrategy.SimulatedSequence)
    @IntAttribute(columnName = "CUSTOMER_ID")
    int customerId();

    @StringAttribute(columnName = "FIRST_NAME", maxLength = 200, nullable = true)
    String firstName();

    @StringAttribute(columnName = "LAST_NAME", maxLength = 200, nullable = true)
    String lastName();

    @StringAttribute(columnName = "COUNTRY", maxLength = 200, nullable = true)
    String country();

    @Relationship(
        cardinality = Relationship.Cardinality.OneToMany,
        contract = "this.customerId = CustomerAccount.customerId",
        relatedIsDependent =  false,
        reverseRelationshipName = "customer"

    )
    CustomerAccountSpec accounts();
}
