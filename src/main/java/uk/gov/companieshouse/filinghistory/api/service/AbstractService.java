package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

public abstract class AbstractService {

    public abstract void saveOrDeleteDocument(FilingHistoryDocument document, Boolean isDelete);

    public abstract ApiResponse<Void> callResourceChangedApi(FilingHistoryDocument documentToSaveDelete,
            Boolean isDelete);

    public abstract void handleResponseAndApplyCompensatoryTransaction(
            ApiResponse<Void> result, FilingHistoryDocument documentToSave,
            FilingHistoryDocument originalDocumentCopy);
}
