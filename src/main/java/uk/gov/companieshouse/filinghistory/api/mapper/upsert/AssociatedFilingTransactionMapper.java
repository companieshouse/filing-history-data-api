package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

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

    private static final String MISSING_ENTITY_ID_ERROR_MSG =
            "Child found in MongoDB with no _entity_id; Possible duplicate being persisted with _entity_id: [%s]";

    private final ChildMapper<FilingHistoryAssociatedFiling> associatedFilingChildMapper;
    private final Supplier<Instant> instantSupplier;

    public AssociatedFilingTransactionMapper(LinksMapper linksMapper,
                                             ChildMapper<FilingHistoryAssociatedFiling> associatedFilingChildMapper,
                                             Supplier<Instant> instantSupplier) {
        super(linksMapper);
        this.associatedFilingChildMapper = associatedFilingChildMapper;
        this.instantSupplier = instantSupplier;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return data.associatedFilings(List.of(
                associatedFilingChildMapper.mapChild(new FilingHistoryAssociatedFiling(), request)));
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
                                                                               FilingHistoryDocument existingDocument) {
        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getAssociatedFilings())
                .ifPresentOrElse(
                        associatedFilingList -> associatedFilingList.stream()
                                .filter(associatedFiling -> requestEntityId.equals(associatedFiling.getEntityId()))
                                .findFirst()
                                .ifPresentOrElse(associatedFiling -> {
                                            if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                    associatedFiling.getDeltaAt())) {
                                                throw new ConflictException(
                                                        "Delta at stale when updating associated filing");
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
        return mapTopLevelFields(request, existingDocument);
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request, FilingHistoryDocument document) {
        final InternalData internalData = request.getInternalData();

        document.getData().paperFiled(request.getExternalData().getPaperFiled());
        return document
                .entityId(internalData.getParentEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .updatedAt(instantSupplier.get())
                .updatedBy(internalData.getUpdatedBy());
    }
}
