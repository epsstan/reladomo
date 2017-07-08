package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import java.sql.Timestamp;

/*
   @PrimaryKey
   @PrimaryKeySimulatedSequenceStrategy(
 */

@com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.ReladomoObject(
    packageName = "com.examples.reladomogen",
    defaultTableName = "CUSTOMER_ACCOUNT"
)
public interface CustomerAccountSpec
{
    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.AsOfAttribute(fromColumnName = "FROM_Z", toColumnName = "THRU_Z",
            toIsInclusive = false, isProcessingDate = false,
            infinityDate = "[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()",
            defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()]",
            futureExpiringRowsExist = true)
    Timestamp businessDate();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.AsOfAttribute(fromColumnName = "IN_Z", toColumnName = "OUT_Z",
            toIsInclusive = false, isProcessingDate = true,
            infinityDate = "[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()",
            defaultIfNotSpecified="[com.gs.fw.common.mithra.generator.annotationprocessor.examples.TimestampProvider.getInfinityDate()]",
            futureExpiringRowsExist = true)
    Timestamp processingDate();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.PrimaryKey()
    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.SimulatedSequencePKStrategy(sequenceName = "A", sequenceObjectFactoryName = "B",
            hasSourceAttribute = false, batchSize = 10, intialValue = 1, incrementSize = 1)
    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.IntAttribute(columnName = "ACCOUNT_ID")
    int accountId();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.IntAttribute(columnName = "CUSTOMER_ID", nullable = false)
    int customerId();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.StringAttribute(columnName = "ACCOUNT_NAME", nullable = false, maxLength = 48)
    String accountName();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.StringAttribute(columnName = "ACCOUNT_TYPE", nullable = false, maxLength = 16)
    String accountType();

    @com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.DoubleAttribute(columnName = "BALANCE", nullable = false)
    double balance();
}
