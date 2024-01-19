package uk.gov.companieshouse.filinghistory.api.mapper;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;

@Component
public class OriginalValuesMapper {

    FilingHistoryOriginalValues map(final InternalDataOriginalValues originalValues) {
        return Optional.ofNullable(originalValues)
                .map(values -> new FilingHistoryOriginalValues()
                        .officerName(values.getOfficerName())
                        .resignationDate(values.getResignationDate()))
                .orElse(null);
    }
}
