package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;
import com.gs.fw.common.mithra.generator.metamodel.CardinalityType;
import com.gs.fw.common.mithra.generator.metamodel.ObjectType;

import java.sql.Timestamp;

@ReladomoObject(
    packageName = "com.examples.reladomogen",
    defaultTableName = "CUSTOMER",
    objectType = ObjectType.Enums.TRANSACTIONAL
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

    @PrimaryKey()
    @MaxPKStrategy()
    @IntAttribute(columnName = "CUSTOMER_ID")
    int customerId();

    @StringAttribute(columnName = "FIRST_NAME", maxLength = 200, nullable = true)
    String firstName();

    @StringAttribute(columnName = "LAST_NAME", maxLength = 200, nullable = true)
    String lastName();

    @StringAttribute(columnName = "COUNTRY", maxLength = 200, nullable = true)
    @Properties({
            @Property(key = "a1", value = "b1"),
            @Property(key = "a2", value = "b2"),
            @Property(key = "a3", value = "b3"),
    })
    String country();

    @Relationship(
        cardinality = CardinalityType.Enums.ONE_TO_MANY,
        contract = "this.customerId = CustomerAccount.customerId",
        relatedIsDependent =  false,
        reverseRelationshipName = "customer"

    )
    CustomerAccountSpec accounts();
}
