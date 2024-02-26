package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryGetResponseProcessor implements GetResponseProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final Service filingHistoryService;
    private final ItemGetResponseMapper itemGetResponseMapper;

    public FilingHistoryGetResponseProcessor(Service filingHistoryService, ItemGetResponseMapper itemGetResponseMapper) {
        this.filingHistoryService = filingHistoryService;
        this.itemGetResponseMapper = itemGetResponseMapper;
    }

    @Override
    public ExternalData processGetSingleFilingHistory(final String transactionId, final String companyNumber) {
        return itemGetResponseMapper.mapFilingHistoryItem(
                filingHistoryService.findExistingFilingHistory(transactionId, companyNumber)
                        .orElseGet(() -> {
                            LOGGER.error("Record with transaction ID could not be found in MongoDB",
                                    DataMapHolder.getLogMap());

                            throw new NotFoundException("Record with this transaction ID could not be found in MongoDB");
                        }));
    }
}
