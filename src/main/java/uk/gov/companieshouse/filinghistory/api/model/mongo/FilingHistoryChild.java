package uk.gov.companieshouse.filinghistory.api.model.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public abstract class FilingHistoryChild {

    @Field("_entity_id")
    @JsonProperty("_entity_id")
    protected String entityId;
    @Field("delta_at")
    @JsonProperty("delta_at")
    protected String deltaAt;

    public String getEntityId() {
        return entityId;
    }

    public abstract FilingHistoryChild entityId(String entityId);

    public String getDeltaAt() {
        return deltaAt;
    }

    public abstract FilingHistoryChild deltaAt(String deltaAt);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilingHistoryChild that = (FilingHistoryChild) o;
        return Objects.equals(entityId, that.entityId) && Objects.equals(deltaAt, that.deltaAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, deltaAt);
    }

    @Override
    public String toString() {
        return "FilingHistoryChild{" +
                "entityId='" + entityId + '\'' +
                ", deltaAt='" + deltaAt + '\'' +
                '}';
    }
}
