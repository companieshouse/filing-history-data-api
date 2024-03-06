package uk.gov.companieshouse.filinghistory.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "company_filing_history")
public class FilingHistoryDocument {

    @Id
    @JsonProperty("_id")
    private String transactionId;
    @Field("_entity_id")
	@JsonProperty("_entity_id")
    private String entityId;
    @Field("company_number")
	@JsonProperty("company_number")
    private String companyNumber;
    @Field("_document_id")
	@JsonProperty("_document_id")
    private String documentId;
    @Field("_barcode")
	@JsonProperty("_barcode")
    private String barcode;
    private FilingHistoryData data;
    @Field("original_description")
	@JsonProperty("original_description")
    private String originalDescription;
    @Field("original_values")
	@JsonProperty("original_values")
    private FilingHistoryOriginalValues originalValues;
    @Field("delta_at")
	@JsonProperty("delta_at")
    private String deltaAt;
    @Field("updated_at")
	@JsonProperty("updated_at")
    private Instant updatedAt;
    @Field("updated_by")
	@JsonProperty("updated_by")
    private String updatedBy;

    public String getTransactionId() {
        return transactionId;
    }

    public FilingHistoryDocument transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

    public FilingHistoryDocument entityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public FilingHistoryDocument companyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public String getDocumentId() {
        return documentId;
    }

    public FilingHistoryDocument documentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getBarcode() {
        return barcode;
    }

    public FilingHistoryDocument barcode(String barcode) {
        this.barcode = barcode;
        return this;
    }

    public FilingHistoryData getData() {
        return data;
    }

    public FilingHistoryDocument data(FilingHistoryData data) {
        this.data = data;
        return this;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public FilingHistoryDocument originalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
        return this;
    }

    public FilingHistoryOriginalValues getOriginalValues() {
        return originalValues;
    }

    public FilingHistoryDocument originalValues(FilingHistoryOriginalValues filingHistoryOriginalValues) {
        this.originalValues = filingHistoryOriginalValues;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public FilingHistoryDocument deltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public FilingHistoryDocument updatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public FilingHistoryDocument updatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilingHistoryDocument that = (FilingHistoryDocument) o;
        return Objects.equals(transactionId, that.transactionId) && Objects.equals(entityId, that.entityId)
                && Objects.equals(companyNumber, that.companyNumber) && Objects.equals(documentId, that.documentId)
                && Objects.equals(barcode, that.barcode) && Objects.equals(data, that.data) && Objects.equals(
                originalDescription, that.originalDescription) && Objects.equals(originalValues, that.originalValues)
                && Objects.equals(deltaAt, that.deltaAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(
                updatedBy, that.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, entityId, companyNumber, documentId, barcode, data, originalDescription,
                originalValues, deltaAt, updatedAt, updatedBy);
    }

    @Override
    public String toString() {
        return "FilingHistoryDocument{" +
                "transactionId='" + transactionId + '\'' +
                ", entityId='" + entityId + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", documentId='" + documentId + '\'' +
                ", barcode='" + barcode + '\'' +
                ", data=" + data +
                ", originalDescription='" + originalDescription + '\'' +
                ", originalValues=" + originalValues +
                ", deltaAt='" + deltaAt + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
