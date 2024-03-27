package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.instantToString;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAltCapital;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryCapital;

@Component
public class CapitalDescriptionGetResponseMapper {

    public List<CapitalDescriptionValue> mapFilingHistoryCapital(List<FilingHistoryCapital> capital) {
        return capital != null ? capital.stream()
                .map(value -> new CapitalDescriptionValue()
                        .currency(value.getCurrency())
                        .date(instantToString(value.getDate()))
                        .figure(value.getFigure()))
                .toList() : null;
    }

    public List<AltCapitalDescriptionValue> mapFilingHistoryAltCapital(List<FilingHistoryAltCapital> capital) {
        return capital != null ? capital.stream()
                .map(value -> new AltCapitalDescriptionValue()
                        .currency(value.getCurrency())
                        .date(instantToString(value.getDate()))
                        .figure(value.getFigure())
                        .description(value.getDescription()))
                .toList() : null;
    }
}
