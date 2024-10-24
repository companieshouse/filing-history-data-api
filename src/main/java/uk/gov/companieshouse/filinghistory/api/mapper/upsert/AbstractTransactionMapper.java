package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static java.lang.Boolean.TRUE;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.time.Instant;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public abstract class AbstractTransactionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

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

    public FilingHistoryDocument mapExistingFilingHistory(
            InternalFilingHistoryApi request, FilingHistoryDocument existingDocument, Instant instant) {
        FilingHistoryData existingData = existingDocument.getData();

        Boolean paperFiled = existingData.getPaperFiled();
        if (!TRUE.equals(paperFiled)) {
            paperFiled = request.getExternalData().getPaperFiled();
        }
        return mapTopLevelFields(request, existingDocument, instant)
                .data(mapFilingHistoryData(request, existingData)
                        .paperFiled(paperFiled));
    }

    public FilingHistoryDocument mapDocumentMetadata(FilingHistoryDocumentMetadataUpdateApi request,
            FilingHistoryDocument existingDocument) {

        FilingHistoryLinks existingLinks = existingDocument.getData().getLinks();
        if (existingLinks == null) {
            LOGGER.info("Existing legacy data with null links object - creating new links object",
                    DataMapHolder.getLogMap());

            existingDocument.getData().links(new FilingHistoryLinks()
                    .self("/company/%s/filing-history/%s"
                            .formatted(existingDocument.getCompanyNumber(), existingDocument.getTransactionId()))
                    .documentMetadata(request.getDocumentMetadata()));
        } else {
            existingLinks.documentMetadata(request.getDocumentMetadata());
        }

        if (request.getPages() > 0) {
            existingDocument.getData().pages(request.getPages());
        }
        return existingDocument;
    }

    protected abstract FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data);

    protected abstract FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
            FilingHistoryDocument document, Instant instant);
}
