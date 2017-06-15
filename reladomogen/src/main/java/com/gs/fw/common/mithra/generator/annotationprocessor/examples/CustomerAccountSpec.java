package com.gs.fw.common.mithra.generator.annotationprocessor.examples;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.DoubleAttributeSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.IntAttributeSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.ReladomoObjectSpec;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.StringAttributeSpec;

@ReladomoObjectSpec(
    packageName = "com.examples.reladomogen",
    defaultTableName = "CUSTOMER_ACCOUNT"
)
public interface CustomerAccountSpec
{
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
