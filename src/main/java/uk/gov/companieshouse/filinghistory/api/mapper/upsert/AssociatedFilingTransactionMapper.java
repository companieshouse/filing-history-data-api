package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
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

    public AssociatedFilingTransactionMapper(LinksMapper linksMapper,
            ChildListMapper<FilingHistoryAssociatedFiling> childListMapper) {
        super(linksMapper);
        this.childListMapper = childListMapper;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        childListMapper.mapChildList(request, data.getAssociatedFilings(), data::associatedFilings);
        return data;
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request, FilingHistoryDocument document,
            Instant instant) {
        final InternalData internalData = request.getInternalData();

        return document
                .entityId(internalData.getParentEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(instant)
                        .by(internalData.getUpdatedBy()));
    }
}
