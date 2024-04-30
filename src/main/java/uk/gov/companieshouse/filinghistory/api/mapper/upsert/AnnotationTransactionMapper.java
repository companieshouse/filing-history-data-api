package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class AnnotationTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final ChildListMapper<FilingHistoryAnnotation> childListMapper;
    private final ChildMapper<FilingHistoryAnnotation> annotationChildMapper;

    public AnnotationTransactionMapper(LinksMapper linksMapper,
                                       DataMapper dataMapper, ChildListMapper<FilingHistoryAnnotation> childListMapper,
                                       ChildMapper<FilingHistoryAnnotation> annotationChildMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.childListMapper = childListMapper;
        this.annotationChildMapper = annotationChildMapper;
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
                                                                               FilingHistoryDocument existingDocument,
                                                                               Instant instant) {
        childListMapper.mapChildList(
                request,
                existingDocument.getData().getAnnotations(),
                existingDocument.getData()::annotations);

        return mapTopLevelFields(request, existingDocument, instant);
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        if (StringUtils.isBlank(request.getInternalData().getParentEntityId())) {
            data = dataMapper.map(request.getExternalData(), data);
        }
        return data.annotations(List.of(annotationChildMapper.mapChild(request)));
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
                                                      FilingHistoryDocument document,
                                                      Instant instant) {
        document.getData().paperFiled(request.getExternalData().getPaperFiled());

        final InternalData internalData = request.getInternalData();

        if (StringUtils.isBlank(internalData.getParentEntityId())) {
            document
                    .entityId(internalData.getEntityId())
                    .barcode(request.getExternalData().getBarcode())
                    .documentId(internalData.getDocumentId())
                    .deltaAt(internalData.getDeltaAt())
                    .matchedDefault(internalData.getMatchedDefault())
                    .originalDescription(internalData.getOriginalDescription());
        } else {
            document.entityId(internalData.getParentEntityId());
        }
        return document
                .companyNumber(internalData.getCompanyNumber())
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(instant)
                        .by(internalData.getUpdatedBy()));
    }
}
