package com.test1.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;

import java.sql.Timestamp;

@ReladomoObject(
        packageName = "com.test1.domain",
        defaultTableName = "CUSTOMER"
)
public interface CustomerSpec
{
    @AsOfAttribute(fromColumnName="FROM_Z", toColumnName="THRU_Z", toIsInclusive = false,
        isProcessingDate = false, futureExpiringRowsExist = true,
        infinityDate = "[com.test1.specs.TimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.test1.specs.TimestampProvider.getInfinityDate()]"
    )
    Timestamp businessDate();

    @AsOfAttribute(fromColumnName="IN_Z", toColumnName="OUT_Z", toIsInclusive = false,
        isProcessingDate = true,
        infinityDate = "[com.test1.specs.TimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[com.test1.specs.TimestampProvider.getInfinityDate()]"
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


    @Index(unique=true, attributes="firstName, lastName")
    void index1();
}
