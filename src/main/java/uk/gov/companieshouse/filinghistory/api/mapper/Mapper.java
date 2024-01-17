package uk.gov.companieshouse.filinghistory.api.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class Mapper {

    public FilingHistoryDocument mapFilingHistory(String id, InternalFilingHistoryApi request) {
        return null;
    }

    public FilingHistoryDocument mapFilingHistory(FilingHistoryDocument document, InternalFilingHistoryApi request) {
        return null;
    }
}
