package uk.gov.companieshouse.filinghistory.api.mapper.upsert;


import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAltCapital;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryCapital;

@Component
public class CapitalDescriptionMapper {

    public List<FilingHistoryCapital> mapCapitalDescriptionValueList(List<CapitalDescriptionValue> capital) {
        return capital != null ? capital.stream()
                .map(value -> new FilingHistoryCapital()
                        .currency(value.getCurrency())
                        .date(stringToInstant(value.getDate()))
                        .figure(value.getFigure()))
                .toList() : null;
    }

    public List<FilingHistoryAltCapital> mapAltCapitalDescriptionValueList(List<AltCapitalDescriptionValue> capital) {
        return capital != null ? capital.stream()
                .map(value -> new FilingHistoryAltCapital()
                        .currency(value.getCurrency())
                        .date(stringToInstant(value.getDate()))
                        .figure(value.getFigure())
                        .description(value.getDescription()))
                .toList() : null;
    }
}
