package annotations_set1;

import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.*;

import java.sql.Timestamp;

@ReladomoObjectSpec(
        packageName = "annotations_set1",
    defaultTableName = "CUSTOMER"
)
public interface CustomerSpec
{
    @AsOfAttributeSpec(fromColumnName="FROM_Z", toColumnName="THRU_Z", toIsInclusive = false,
        isProcessingDate = false, futureExpiringRowsExist = true,
        infinityDate="[annotations_set1.TimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[annotations_set1.TimestampProvider.getInfinityDate()]"
    )
    Timestamp businessDate();

    @AsOfAttributeSpec(fromColumnName="IN_Z", toColumnName="OUT_Z", toIsInclusive = false,
        isProcessingDate = true,
        infinityDate="[annotations_set1.TimestampProvider.getInfinityDate()]",
        defaultIfNotSpecified="[annotations_set1.TimestampProvider.getInfinityDate()]"
    )
    Timestamp processingDate();

    @PrimaryKeySpec()
    @IntAttributeSpec(columnName = "CUSTOMER_ID")
    int customerId();

    @StringAttributeSpec(columnName = "FIRST_NAME", maxLength = 200, nullable = true)
    String firstName();

    @StringAttributeSpec(columnName = "LAST_NAME", maxLength = 200, nullable = true)
    String lastName();

    @StringAttributeSpec(columnName = "COUNTRY", maxLength = 200, nullable = true)
    String country();

    /*
    @RelationshipSpec(
        cardinality = RelationshipSpec.Cardinality.OneToMany,
        contract = "this.customerId == CustomerAccount.customerId",
        relatedIsDependent =  false,
        reverseRelationshipName = "customer"

    )
    CustomerAccountSpec accounts();
    */
}
