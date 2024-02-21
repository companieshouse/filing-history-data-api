package uk.gov.companieshouse.filinghistory.api.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.InternalServerErrorException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryDocumentCopier {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    private final ObjectMapper objectMapper;

    public FilingHistoryDocumentCopier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FilingHistoryDocument deepCopy(FilingHistoryDocument originalDocument) {
        try {
            return objectMapper.readValue(
                    objectMapper.writeValueAsString(originalDocument), FilingHistoryDocument.class);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Failed to serialise/deserialise Filing History document", ex, DataMapHolder.getLogMap());
            throw new InternalServerErrorException("Failed to serialise/deserialise Filing History document");
        }
    }
}
