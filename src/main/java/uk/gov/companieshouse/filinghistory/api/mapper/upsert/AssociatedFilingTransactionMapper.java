package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.makeNewTimeStampObject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class AssociatedFilingTransactionMapper extends AbstractTransactionMapper {

    private final ChildMapper<FilingHistoryAssociatedFiling> associatedFilingChildMapper;

    public AssociatedFilingTransactionMapper(LinksMapper linksMapper,
            ChildMapper<FilingHistoryAssociatedFiling> associatedFilingChildMapper) {
        super(linksMapper);
        this.associatedFilingChildMapper = associatedFilingChildMapper;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return data.associatedFilings(List.of(
                associatedFilingChildMapper.mapChild(new FilingHistoryAssociatedFiling(), request)));
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument,
            Instant instant) {
        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getAssociatedFilings())
                .ifPresentOrElse(
                        associatedFilingList -> associatedFilingList.stream()
                                .filter(associatedFiling -> requestEntityId.equals(associatedFiling.getEntityId()))
                                .findFirst()
                                .ifPresentOrElse(associatedFiling -> {
                                            if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                    associatedFiling.getDeltaAt())) {
                                                LOGGER.error(STALE_DELTA_ERROR_MESSAGE.formatted(
                                                                request.getInternalData().getDeltaAt(),
                                                                associatedFiling.getDeltaAt()),
                                                        DataMapHolder.getLogMap());
                                                throw new ConflictException(
                                                        "Stale delta when updating associated filing");
                                            }
                                            // Update already existing associated filing from existing list
                                            associatedFilingChildMapper.mapChild(associatedFiling, request);
                                        },
                                        // Add new associated filing to existing list
                                        () -> {
                                            if (associatedFilingList.stream()
                                                    .anyMatch(associatedFiling ->
                                                            StringUtils.isBlank(associatedFiling.getEntityId()))) {
                                                LOGGER.info(
                                                        MISSING_ENTITY_ID_ERROR_MSG.formatted(requestEntityId),
                                                        DataMapHolder.getLogMap()
                                                );
                                            }
                                            associatedFilingList
                                                    .add(associatedFilingChildMapper
                                                            .mapChild(new FilingHistoryAssociatedFiling(), request));
                                        }),
                        // Add new associated filing to a new associated filing list
                        () -> mapFilingHistoryData(request, existingDocument.getData())
                );
        return mapTopLevelFields(request, existingDocument, instant);
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
                .updated(makeNewTimeStampObject(instant, internalData.getUpdatedBy()));
    }
}
