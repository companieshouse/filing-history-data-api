package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class ResolutionTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final ChildListMapper<FilingHistoryResolution> childListMapper;
    private final ChildMapper<FilingHistoryResolution> resolutionChildMapper;

    public ResolutionTransactionMapper(LinksMapper linksMapper,
                                       DataMapper dataMapper,
                                       ChildListMapper<FilingHistoryResolution> childListMapper,
                                       ChildMapper<FilingHistoryResolution> resolutionChildMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.childListMapper = childListMapper;
        this.resolutionChildMapper = resolutionChildMapper;
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
                                                                               FilingHistoryDocument existingDocument,
                                                                               Instant instant) {

        childListMapper.mapChildList(
                request,
                existingDocument.getData().getResolutions(),
                existingDocument.getData()::resolutions);

        return mapTopLevelFields(request, existingDocument, instant);
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return dataMapper.map(request.getExternalData(), data)
                .resolutions(List.of(resolutionChildMapper.mapChild(request)));
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
                                                      FilingHistoryDocument document, Instant instant) {
        final InternalData internalData = request.getInternalData();

        document.getData().paperFiled(request.getExternalData().getPaperFiled());
        document.getData().date(stringToInstant(request.getExternalData().getDate()));
        return document
                .companyNumber(internalData.getCompanyNumber())
                .entityId(internalData.getEntityId())
                .deltaAt(internalData.getDeltaAt())
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(instant)
                        .by(internalData.getUpdatedBy()))
                .barcode(request.getExternalData().getBarcode())
                .originalDescription(internalData.getOriginalDescription());
    }
}
