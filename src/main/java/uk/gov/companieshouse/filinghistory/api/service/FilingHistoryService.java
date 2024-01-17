package uk.gov.companieshouse.filinghistory.api.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.mapper.TopLevelMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@Component
public class FilingHistoryService implements Service {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneId.of("Z"));
    private final TopLevelMapper topLevelMapper;
    private final Repository repository;

    public FilingHistoryService(TopLevelMapper topLevelMapper, Repository repository) {
        this.topLevelMapper = topLevelMapper;
        this.repository = repository;
    }

    @Override
    public ServiceResult upsertFilingHistory(final String transactionId, final InternalFilingHistoryApi request) {
        return repository.findById(transactionId)
                .map(existingDocument -> updateFilingHistory(request, existingDocument))
                .orElseGet(() -> insertFilingHistory(transactionId, request));
    }

    private ServiceResult updateFilingHistory(final InternalFilingHistoryApi request, final FilingHistoryDocument existingDocument) {
        if (isDeltaStale(request, existingDocument)) {
            return ServiceResult.STALE_DELTA;
        }
        repository.save(topLevelMapper.mapFilingHistory(existingDocument, request));
        return ServiceResult.UPSERT_SUCCESSFUL;
    }

    private ServiceResult insertFilingHistory(final String transactionId, final InternalFilingHistoryApi request) {
        repository.save(topLevelMapper.mapFilingHistory(transactionId, request));
        return ServiceResult.UPSERT_SUCCESSFUL;
    }

    private static boolean isDeltaStale(final InternalFilingHistoryApi request, final FilingHistoryDocument existingDocument) {
        return !request.getInternalData().getDeltaAt()
                .isAfter(OffsetDateTime.parse(existingDocument.getDeltaAt(), FORMATTER));
    }
}
