package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class CompositeResolutionDeleteMapper implements DeleteMapper {

    @Override
    public Optional<FilingHistoryDocument> removeTransaction(int index, FilingHistoryDocument existingDocument) {
        FilingHistoryData data = existingDocument.getData();
        List<FilingHistoryResolution> resolutions = data.getResolutions();
        resolutions.remove(index);
        if (resolutions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(existingDocument);
    }
}