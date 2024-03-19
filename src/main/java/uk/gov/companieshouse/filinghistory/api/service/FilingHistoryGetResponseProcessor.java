package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ListGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListParams;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryGetResponseProcessor implements GetResponseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final Pattern STATUS_NOT_AVAILABLE_PATTERN = Pattern.compile("^filing-history-not-available(?!.*before)");

    private final Service filingHistoryService;
    private final ItemGetResponseMapper itemGetResponseMapper;
    private final StatusService statusService;
    private final ListGetResponseMapper listGetResponseMapper;

    public FilingHistoryGetResponseProcessor(Service filingHistoryService, ItemGetResponseMapper itemGetResponseMapper,
            StatusService statusService, ListGetResponseMapper listGetResponseMapper) {
        this.filingHistoryService = filingHistoryService;
        this.itemGetResponseMapper = itemGetResponseMapper;
        this.statusService = statusService;
        this.listGetResponseMapper = listGetResponseMapper;
    }

    @Override
    public ExternalData processGetSingleFilingHistory(final String transactionId, final String companyNumber) {
        return itemGetResponseMapper.mapFilingHistoryItem(
                filingHistoryService.findExistingFilingHistory(transactionId, companyNumber)
                        .orElseGet(() -> {
                            LOGGER.error("Record could not be found in MongoDB",
                                    DataMapHolder.getLogMap());

                            throw new NotFoundException("Record could not be found in MongoDB");
                        }));
    }

    @Override
    public FilingHistoryList processGetCompanyFilingHistoryList(FilingHistoryListParams params) {

        // calc itemsPerPage and startIndex

        // get filingHistoryStatus
        String status = statusService.processStatus(params.companyNumber());
        // build base FilingHistoryList response
        FilingHistoryList baseResponse = listGetResponseMapper.mapBaseFilingHistoryList(params.startIndex(), params.itemsPerPage(), status);

        // return if status matches ^filing-history-not-available(?!.*before)
        Matcher statusMatcher = STATUS_NOT_AVAILABLE_PATTERN.matcher(status);
        if (statusMatcher.find()) {
            return baseResponse;
        }

        // build category filter, if confirmation-statement then also annual-return

        // get query count
        // get docs with filter, limit, skip and sort
        // map documents

        return null;
    }
}
