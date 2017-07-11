package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.*;

import java.sql.Timestamp;

/*
   @PrimaryKey
   @PrimaryKeySimulatedSequenceStrategy(
 */

@ReladomoObject(
    packageName = "com.examples.reladomogen",
    defaultTableName = "CUSTOMER_ACCOUNT"
)
public interface CustomerAccountSpec
{
    @AsOfAttribute(fromColumnName = "FROM_Z", toColumnName = "THRU_Z",
            toIsInclusive = false, isProcessingDate = false,
            infinityDate = "[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()",
            defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()]",
            futureExpiringRowsExist = true)
    Timestamp businessDate();

    @AsOfAttribute(fromColumnName = "IN_Z", toColumnName = "OUT_Z",
            toIsInclusive = false, isProcessingDate = true,
            infinityDate = "[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()",
            defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()]",
            futureExpiringRowsExist = true)
    Timestamp processingDate();

    @PrimaryKey()
    @SimulatedSequencePKStrategy(sequenceName = "A", sequenceObjectFactoryName = "B",
            hasSourceAttribute = false, batchSize = 10, intialValue = 1, incrementSize = 1)
    @IntAttribute(columnName = "ACCOUNT_ID")
    int accountId();

    @IntAttribute(columnName = "CUSTOMER_ID", nullable = false)
    int customerId();

    @StringAttribute(columnName = "ACCOUNT_NAME", nullable = false, maxLength = 48)
    String accountName();

    @StringAttribute(columnName = "ACCOUNT_TYPE", nullable = false, maxLength = 16)
    String accountType();

    @DoubleAttribute(columnName = "BALANCE", nullable = false)
    double balance();
}
