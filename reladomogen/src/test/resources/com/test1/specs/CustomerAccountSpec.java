package com.test1.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;

import java.sql.Timestamp;

@ReladomoObjectSpec(
    packageName = "com.test1.domain",
    defaultTableName = "CUSTOMER_ACCOUNT"
)
public interface CustomerAccountSpec
{
    @AsOfAttributeSpec(fromColumnName = "FROM_Z", toColumnName = "THRU_Z",
            toIsInclusive = false, isProcessingDate = false,
            infinityDate = "[com.test1.specs.TimestampProvider.getInfinityDate()]",
            defaultIfNotSpecified="[com.test1.specs.TimestampProvider.getInfinityDate()]",
            futureExpiringRowsExist = true)
    Timestamp businessDate();

    @AsOfAttributeSpec(fromColumnName = "IN_Z", toColumnName = "OUT_Z",
            toIsInclusive = false, isProcessingDate = true,
            infinityDate = "[com.test1.specs.TimestampProvider.getInfinityDate()]",
            defaultIfNotSpecified="[com.test1.specs.TimestampProvider.getInfinityDate()]",
            futureExpiringRowsExist = true)
    Timestamp processingDate();

    @PrimaryKeySpec()
    @IntAttributeSpec(columnName = "ACCOUNT_ID")
    int accountId();

    @IntAttributeSpec(columnName = "CUSTOMER_ID", nullable = false)
    int customerId();

    @StringAttributeSpec(columnName = "ACCOUNT_NAME", nullable = false, maxLength = 48)
    String accountName();

    @StringAttributeSpec(columnName = "ACCOUNT_TYPE", nullable = false, maxLength = 16)
    String accountType();

    @DoubleAttributeSpec(columnName = "BALANCE", nullable = false)
    double balance();
}
