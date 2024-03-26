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
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListRequestParams;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryGetResponseProcessor implements GetResponseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final int MAX_ITEMS_PER_PAGE = 100;
    private static final Pattern STATUS_NOT_AVAILABLE_PATTERN =
            Pattern.compile("^filing-history-not-available(?!.*before)");

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
                            LOGGER.error("Record could not be found in MongoDB", DataMapHolder.getLogMap());
                            throw new NotFoundException("Record could not be found in MongoDB");
                        }));
    }

    @Override
    public FilingHistoryList processGetCompanyFilingHistoryList(FilingHistoryListRequestParams requestParams) {
        final String companyNumber = requestParams.companyNumber();
        final int itemsPerPage = Math.min(requestParams.itemsPerPage(), MAX_ITEMS_PER_PAGE);
        final int startIndex = requestParams.startIndex();

        String status = statusService.processStatus(companyNumber);
        DataMapHolder.get().status(status);
        Matcher statusMatcher = STATUS_NOT_AVAILABLE_PATTERN.matcher(status);

        FilingHistoryList baseResponse = listGetResponseMapper.mapBaseFilingHistoryList(startIndex, itemsPerPage,
                status);
        if (statusMatcher.find()) {
            LOGGER.info("Filing history has status not available", DataMapHolder.getLogMap());
            return baseResponse;
        }

        return filingHistoryService.findCompanyFilingHistoryList(companyNumber, startIndex, itemsPerPage,
                        requestParams.categories())
                .map(listAggregate -> listGetResponseMapper.mapFilingHistoryList(startIndex, itemsPerPage, status,
                        listAggregate))
                .orElseGet(() -> {
                    LOGGER.error("Company filing history not found", DataMapHolder.getLogMap());
                    return baseResponse;
                });
    }
}
