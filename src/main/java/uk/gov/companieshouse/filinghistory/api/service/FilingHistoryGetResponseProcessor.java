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
    public ExternalData processGetSingleFilingHistory(String companyNumber, String transactionId) {
        return itemGetResponseMapper.mapFilingHistoryItem(
                filingHistoryService.findExistingFilingHistory(transactionId)
                        .orElseGet(() -> {
                            LOGGER.error("Record with transaction ID: [%s] could not be found in MongoDB"
                                    .formatted(transactionId), DataMapHolder.getLogMap());

                            throw new NotFoundException("Record with transaction ID: [%s] could not be found in MongoDB"
                                    .formatted(transactionId));
                        }));
    }
}
