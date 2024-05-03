package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public abstract class AbstractTransactionMapper {

    private final LinksMapper linksMapper;

    protected AbstractTransactionMapper(LinksMapper linksMapper) {
        this.linksMapper = linksMapper;
    }

    public FilingHistoryDocument mapNewFilingHistory(String id, InternalFilingHistoryApi request, Instant instant) {
        FilingHistoryDocument newDocument = new FilingHistoryDocument()
                .transactionId(id)
                .created(new FilingHistoryDeltaTimestamp()
                        .at(instant)
                        .by(request.getInternalData().getUpdatedBy()));

        return mapTopLevelFields(request, newDocument, instant)
                .data(mapFilingHistoryData(request, new FilingHistoryData())
                        .links(linksMapper.map(request.getExternalData().getLinks()))
                        .paperFiled(request.getExternalData().getPaperFiled()));
    }

    public FilingHistoryDocument mapExistingFilingHistory(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument, Instant instant) {
        return mapTopLevelFields(request, existingDocument, instant)
                .data(mapFilingHistoryData(request, existingDocument.getData())
                        .paperFiled(request.getExternalData().getPaperFiled()));
    }

    protected abstract FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data);

    protected abstract FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
            FilingHistoryDocument document, Instant instant);
}
