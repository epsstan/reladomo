package com.test1.specs;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;
import com.gs.fw.common.mithra.generator.metamodel.CardinalityType;
import com.gs.fw.common.mithra.generator.metamodel.PrimaryKeyGeneratorStrategyType;
import com.gs.fw.common.mithra.generator.metamodel.ObjectType;

import java.sql.Timestamp;

@ReladomoObject(
    packageName = "com.test1.domain",
    defaultTableName = "CUSTOMER_ACCOUNT",
    objectType = ObjectType.Enums.TRANSACTIONAL
)
public interface CustomerAccountSpec
{
    @AsOfAttribute(fromColumnName = "FROM_Z", toColumnName = "THRU_Z",
            toIsInclusive = false, isProcessingDate = false,
            infinityDate = "[com.test1.specs.TimestampProvider.getInfinityDate()]",
            defaultIfNotSpecified="[com.test1.specs.TimestampProvider.getInfinityDate()]",
            futureExpiringRowsExist = true)
    Timestamp businessDate();

    @AsOfAttribute(fromColumnName = "IN_Z", toColumnName = "OUT_Z",
            toIsInclusive = false, isProcessingDate = true,
            infinityDate = "[com.test1.specs.TimestampProvider.getInfinityDate()]",
            defaultIfNotSpecified="[com.test1.specs.TimestampProvider.getInfinityDate()]",
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

    @BigDecimalAttribute(columnName = "BIG_DECIMAL", nullable = false, precision=1, scale=1)
    double bigDecimalAttribute();

    @FloatAttribute(columnName = "FLOAT", nullable = false)
    float floatAttribute();

    @CharAttribute(columnName = "CHAR", nullable = false)
    char charAttribute();

    @ByteAttribute(columnName = "BYTE", nullable = false)
    byte byteAttribute();

    @ByteArrayAttribute(columnName = "BYTE_ARRAY", nullable = false)
    char byteArrayAttribute();

    @IntAttribute(columnName = "a", nullable = false)
    int a();
}
