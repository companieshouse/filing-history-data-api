package uk.gov.companieshouse.filinghistory.api.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

import java.time.Instant;
import java.time.ZoneId;

@Component
public class DataMapper {

    public FilingHistoryData mapFilingHistoryExternalData(ExternalData externalData) {
        return new FilingHistoryData()
                .type(externalData.getType())
                .date(externalData.getDate().atStartOfDay(ZoneId.of("Z")).toInstant())
                .category(externalData.getCategory().toString())
                .subcategory(externalData.getSubcategory().toString())
                .description(externalData.getDescription())
                .descriptionValues(new FilingHistoryDescriptionValues()
                        .terminationDate(Instant.parse(externalData.getDescriptionValues().getTerminationDate()))
                        .officerName(externalData.getDescriptionValues().getOfficerName()))
                .actionDate(externalData.getActionDate());
    }
}
