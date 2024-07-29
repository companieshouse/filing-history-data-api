package uk.gov.companieshouse.filinghistory.api.model.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "company_filing_history")
public class UnversionedFilingHistoryDocument {

    @Id
    @JsonProperty("_id")
    private String transactionId;
    private Long version = 0L;
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
    private FilingHistoryDeltaTimestamp updated;
    private FilingHistoryDeltaTimestamp created;
    @Field("matched_default")
    @JsonProperty("matched_default")
    private Integer matchedDefault;

    public UnversionedFilingHistoryDocument() {
    }

    public UnversionedFilingHistoryDocument(FilingHistoryDocument copy) {
        this.transactionId = copy.getTransactionId();
        this.version = 0L;
        this.entityId = copy.getEntityId();
        this.companyNumber = copy.getCompanyNumber();
        this.documentId = copy.getDocumentId();
        this.barcode = copy.getBarcode();
        this.data = copy.getData();
        this.originalDescription = copy.getOriginalDescription();
        this.originalValues = copy.getOriginalValues();
        this.deltaAt = copy.getDeltaAt();
        this.updated = copy.getUpdated();
        this.created = copy.getCreated();
        this.matchedDefault = copy.getMatchedDefault();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public UnversionedFilingHistoryDocument transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public UnversionedFilingHistoryDocument version(long version) {
        this.version = version;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

    public UnversionedFilingHistoryDocument entityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public UnversionedFilingHistoryDocument companyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public String getDocumentId() {
        return documentId;
    }

    public UnversionedFilingHistoryDocument documentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getBarcode() {
        return barcode;
    }

    public UnversionedFilingHistoryDocument barcode(String barcode) {
        this.barcode = barcode;
        return this;
    }

    public FilingHistoryData getData() {
        return data;
    }

    public UnversionedFilingHistoryDocument data(FilingHistoryData data) {
        this.data = data;
        return this;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public UnversionedFilingHistoryDocument originalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
        return this;
    }

    public FilingHistoryOriginalValues getOriginalValues() {
        return originalValues;
    }

    public UnversionedFilingHistoryDocument originalValues(FilingHistoryOriginalValues filingHistoryOriginalValues) {
        this.originalValues = filingHistoryOriginalValues;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public UnversionedFilingHistoryDocument deltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public FilingHistoryDeltaTimestamp getUpdated() {
        return updated;
    }

    public UnversionedFilingHistoryDocument updated(FilingHistoryDeltaTimestamp updated) {
        this.updated = updated;
        return this;
    }

    public FilingHistoryDeltaTimestamp getCreated() {
        return created;
    }

    public UnversionedFilingHistoryDocument created(FilingHistoryDeltaTimestamp created) {
        this.created = created;
        return this;
    }

    public Integer getMatchedDefault() {
        return matchedDefault;
    }

    public UnversionedFilingHistoryDocument matchedDefault(Integer matchedDefault) {
        this.matchedDefault = matchedDefault;
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
        UnversionedFilingHistoryDocument that = (UnversionedFilingHistoryDocument) o;
        return Objects.equals(transactionId, that.transactionId) && Objects.equals(version,
                that.version) && Objects.equals(entityId, that.entityId) && Objects.equals(
                companyNumber, that.companyNumber) && Objects.equals(documentId, that.documentId)
                && Objects.equals(barcode, that.barcode) && Objects.equals(data, that.data)
                && Objects.equals(originalDescription, that.originalDescription) && Objects.equals(
                originalValues, that.originalValues) && Objects.equals(deltaAt, that.deltaAt)
                && Objects.equals(updated, that.updated) && Objects.equals(created, that.created)
                && Objects.equals(matchedDefault, that.matchedDefault);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, version, entityId, companyNumber, documentId, barcode, data,
                originalDescription,
                originalValues, deltaAt, updated, created, matchedDefault);
    }

    @Override
    public String toString() {
        return "UnversionedFilingHistoryDocument{" +
                "transactionId='" + transactionId + '\'' +
                ", version=" + version +
                ", entityId='" + entityId + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", documentId='" + documentId + '\'' +
                ", barcode='" + barcode + '\'' +
                ", data=" + data +
                ", originalDescription='" + originalDescription + '\'' +
                ", originalValues=" + originalValues +
                ", deltaAt='" + deltaAt + '\'' +
                ", updated=" + updated +
                ", created=" + created +
                ", matchedDefault=" + matchedDefault +
                '}';
    }
}
