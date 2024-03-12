package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.ArrayList;
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
        return data.associatedFilings(associatedFilingListMapper.addNewChildToList(new ArrayList<>(), request));
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryUnlessStale(InternalFilingHistoryApi request, FilingHistoryDocument existingDocument) {
        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getAssociatedFilings())
                .ifPresentOrElse(
                        associatedFilingList ->
                                associatedFilingList.stream()
                                        .filter(associatedFiling -> associatedFiling.getEntityId().equals(requestEntityId))
                                        .findFirst()
                                        .ifPresentOrElse(associatedFiling -> {
                                                    if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                            associatedFiling.getDeltaAt())) {
                                                        throw new ConflictException(
                                                                "Delta at stale when upserting associated filing");
                                                    }
                                                    associatedFilingListMapper.updateExistingChild(associatedFiling, request);
                                                },
                                                () -> associatedFilingListMapper
                                                        .addNewChildToList(associatedFilingList, request)),
                        () -> existingDocument.getData().associatedFilings(
                                associatedFilingListMapper.addNewChildToList(new ArrayList<>(), request))
                );
        return mapFilingHistory(request, existingDocument);
    }

    @Override
    protected FilingHistoryDocument mapFilingHistory(InternalFilingHistoryApi request, FilingHistoryDocument document) {
        final InternalData internalData = request.getInternalData();

        document.getData().paperFiled(request.getExternalData().getPaperFiled());
        return document
                .entityId(internalData.getParentEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .updatedAt(instantSupplier.get())
                .updatedBy(internalData.getUpdatedBy());
    }
}
