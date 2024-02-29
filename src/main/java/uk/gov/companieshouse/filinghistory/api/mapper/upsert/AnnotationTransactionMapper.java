package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

public class AnnotationTransactionMapper extends AbstractTransactionMapper {
    protected AnnotationTransactionMapper(LinksMapper linksMapper) {
        super(linksMapper);
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(ExternalData externalData, FilingHistoryData existingData) {
        return null;
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryUnlessStale(InternalFilingHistoryApi request, FilingHistoryDocument existingDocument) {
        return null;
    }

    @Override
    protected FilingHistoryDocument mapFilingHistory(InternalFilingHistoryApi request, FilingHistoryDocument document) {
        return null;
    }
}
