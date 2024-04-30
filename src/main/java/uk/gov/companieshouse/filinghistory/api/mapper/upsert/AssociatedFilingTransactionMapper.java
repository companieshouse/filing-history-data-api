package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class AssociatedFilingTransactionMapper extends AbstractTransactionMapper {

    private final ChildListMapper<FilingHistoryAssociatedFiling> childListMapper;
    private final ChildMapper<FilingHistoryAssociatedFiling> associatedFilingChildMapper;

    public AssociatedFilingTransactionMapper(LinksMapper linksMapper,
                                             ChildListMapper<FilingHistoryAssociatedFiling> childListMapper,
                                             ChildMapper<FilingHistoryAssociatedFiling> associatedFilingChildMapper) {
        super(linksMapper);
        this.childListMapper = childListMapper;
        this.associatedFilingChildMapper = associatedFilingChildMapper;
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
                                                                               final FilingHistoryDocument existingDocument,
                                                                               Instant instant) {
        childListMapper.mapChildList(request, existingDocument.getData().getAssociatedFilings(),
                existingDocument.getData()::associatedFilings);
        return mapTopLevelFields(request, existingDocument, instant);
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return data.associatedFilings(List.of(associatedFilingChildMapper.mapChild(request)));
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
                                                      FilingHistoryDocument document,
                                                      Instant instant) {
        final InternalData internalData = request.getInternalData();

        document.getData().paperFiled(request.getExternalData().getPaperFiled());
        return document
                .entityId(internalData.getParentEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(instant)
                        .by(internalData.getUpdatedBy()));
    }
}
