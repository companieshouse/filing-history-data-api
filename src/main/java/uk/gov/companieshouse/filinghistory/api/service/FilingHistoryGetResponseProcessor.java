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
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListAggregate;
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
                            LOGGER.error("Record could not be found in MongoDB",
                                    DataMapHolder.getLogMap());

                            throw new NotFoundException("Record could not be found in MongoDB");
                        }));
    }

    @Override
    public FilingHistoryList processGetCompanyFilingHistoryList(FilingHistoryListRequestParams requestParams) {
        final String companyNumber = requestParams.companyNumber();
        final int itemsPerPage = Math.min(requestParams.itemsPerPage(), MAX_ITEMS_PER_PAGE);
        final int startIndex = requestParams.startIndex();

        String status = statusService.processStatus(companyNumber);

        Matcher statusMatcher = STATUS_NOT_AVAILABLE_PATTERN.matcher(status);
        if (statusMatcher.find()) {
            return listGetResponseMapper.mapBaseFilingHistoryList(startIndex, itemsPerPage, status);
        }

        FilingHistoryListAggregate listAggregate =
                filingHistoryService.findCompanyFilingHistoryList(companyNumber, startIndex, itemsPerPage,
                                requestParams.categories())
                        .orElseGet(() -> {
                            LOGGER.error("Company filing history not be found", DataMapHolder.getLogMap());
                            throw new NotFoundException("Company filing history not be found");
                        });

        return listGetResponseMapper.mapFilingHistoryList(startIndex, itemsPerPage, status, listAggregate);
    }
}
