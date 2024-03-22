package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryOriginalValues;

@Component
public class OriginalValuesMapper {

    FilingHistoryOriginalValues map(final InternalDataOriginalValues originalValues) {
        return Optional.ofNullable(originalValues)
                .map(values -> new FilingHistoryOriginalValues()
                        .officerName(values.getOfficerName())
                        .resignationDate(values.getResignationDate())
                        .chargeCreationDate(stringToInstant(values.getChargeCreationDate()))
                        .propertyAcquiredDate(stringToInstant(values.getPropertyAcquiredDate()))
                        .appointmentDate(values.getAppointmentDate())
                        .caseStartDate(values.getCaseStartDate())
                        .caseEndDate(values.getCaseEndDate())
                        .madeUpDate(values.getMadeUpDate())
                        .accType(values.getAccType())
                        .changeDate(values.getChangeDate())
                        .accountingPeriod(values.getAccountingPeriod())
                        .periodType(values.getPeriodType())
                        .newDate(values.getNewDate())
                        .notificationDate(values.getNotificationDate())
                        .pscName(values.getPscName())
                        .newRoAddress(values.getNewRoAddress())
                        .resType(values.getResType())
                        .cessationDate(values.getCessationDate())
                        .action(values.getAction())
                        .capitalType(values.getCapitalType())
                        .mortgageSatisfactionDate(values.getMortgageSatisfactionDate()))
                .orElse(null);
    }
}
