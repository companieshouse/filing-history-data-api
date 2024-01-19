package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;

@Component
public class DataMapper {

    public FilingHistoryData mapFilingHistoryExternalData(ExternalData externalData) {
        return new FilingHistoryData()
                .type(externalData.getType())
                .date(DateUtils.localDateToInstant(externalData.getDate()))
                .category(externalData.getCategory().toString())
                .subcategory(externalData.getSubcategory().toString())
                .description(externalData.getDescription())
                .descriptionValues(new FilingHistoryDescriptionValues()
                        .terminationDate(DateUtils.localDateToInstant(externalData.getDescriptionValues().getTerminationDate()))
                        .officerName(externalData.getDescriptionValues().getOfficerName()))
                .actionDate(Instant.from(externalData.getActionDate().atStartOfDay(ZoneOffset.UTC)))
                .pages(externalData.getPages())
                .paperFiled(externalData.getPaperFiled())
                .links(new FilingHistoryLinks()
                        .documentMetadata(externalData.getLinks().getDocumentMetadata())
                        .self(externalData.getLinks().getSelf()));
    }
}
