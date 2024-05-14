package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public interface DeleteMapper {

    Optional<FilingHistoryDocument> removeTransaction(int index, FilingHistoryDocument existingDocument);
}
