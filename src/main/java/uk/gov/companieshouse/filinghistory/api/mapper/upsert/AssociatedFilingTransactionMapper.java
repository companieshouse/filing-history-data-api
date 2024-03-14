package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class AssociatedFilingTransactionMapper extends AbstractTransactionMapper {

    private final ChildListMapper<FilingHistoryAssociatedFiling> associatedFilingListMapper;
    private final Supplier<Instant> instantSupplier;

    public AssociatedFilingTransactionMapper(LinksMapper linksMapper,
                                             ChildListMapper<FilingHistoryAssociatedFiling> associatedFilingListMapper,
                                             Supplier<Instant> instantSupplier) {
        super(linksMapper);
        this.associatedFilingListMapper = associatedFilingListMapper;
        this.instantSupplier = instantSupplier;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return data.associatedFilings(List.of(
                associatedFilingListMapper.mapChild(new FilingHistoryAssociatedFiling(), request)));
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request, FilingHistoryDocument existingDocument) {
        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getAssociatedFilings())
                .ifPresentOrElse(
                        associatedFilingList -> associatedFilingList.stream()
                                .filter(associatedFiling -> associatedFiling.getEntityId().equals(requestEntityId))
                                .findFirst()
                                .ifPresentOrElse(associatedFiling -> {
                                            if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                    associatedFiling.getDeltaAt())) {
                                                throw new ConflictException(
                                                        "Delta at stale when upserting associated filing");
                                            }
                                            // Update already existing associated filing from existing list
                                            associatedFilingListMapper.mapChild(associatedFiling, request);
                                        },
                                        // Add new associated filing to existing list
                                        () -> associatedFilingList
                                                .add(associatedFilingListMapper
                                                        .mapChild(new FilingHistoryAssociatedFiling(), request))),
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
