package uk.gov.companieshouse.filinghistory.api.mapper.get;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@Component
public class DescriptionGetValuesResponseMapper {

    public FilingHistoryItemDataDescriptionValues map(FilingHistoryDescriptionValues descriptionValues) {
        return new FilingHistoryItemDataDescriptionValues()
                .appointmentDate(convertInstantToString(descriptionValues.getAppointmentDate()))
                .branchNumber(descriptionValues.getBranchNumber())
                .broughtDownDate(convertInstantToString(descriptionValues.getBroughtDownDate()))
                .caseEndDate(convertInstantToString(descriptionValues.getCaseEndDate()))
                .caseNumber(descriptionValues.getCaseNumber())
                .cessationDate(convertInstantToString(descriptionValues.getCessationDate()))
                .changeAddress(descriptionValues.getChangeAddress())
                .changeDate(convertInstantToString(descriptionValues.getChangeDate()))
                .changeDetails(descriptionValues.getChangeDetails())
                .changeName(descriptionValues.getChangeName())
                .changeType(descriptionValues.getChangeType())
                .chargeCreationDate(convertInstantToString(descriptionValues.getChargeCreationDate()))
                .chargeNumber(descriptionValues.getChargeNumber())
                .closeDate(convertInstantToString(descriptionValues.getCloseDate()))
                .companyNumber(descriptionValues.getCompanyNumber())
                .companyType(descriptionValues.getCompanyType())
                .date(convertInstantToString(descriptionValues.getDate()))
                .defaultAddress(descriptionValues.getDefaultAddress())
                .description(descriptionValues.getDescription())
                .formAttached(descriptionValues.getFormAttached())
                .formType(descriptionValues.getFormType())
                .incorporationDate(convertInstantToString(descriptionValues.getIncorporationDate()))
                .madeUpDate(convertInstantToString(descriptionValues.getMadeUpDate()))
                .newAddress(descriptionValues.getNewAddress())
                .newDate(convertInstantToString(descriptionValues.getNewDate()))
                .newJurisdiction(descriptionValues.getNewJurisdiction())
                .notificationDate(convertInstantToString(descriptionValues.getNotificationDate()))
                .officerAddress(descriptionValues.getOfficerAddress())
                .officerName(descriptionValues.getOfficerName())
                .terminationDate(convertInstantToString(descriptionValues.getTerminationDate()))
                .oldAddress(descriptionValues.getOldAddress())
                .oldJurisdiction(descriptionValues.getOldJurisdiction())
                .originalDescription(descriptionValues.getOriginalDescription())
                .propertyAcquiredDate(convertInstantToString(descriptionValues.getPropertyAcquiredDate()))
                .pscName(descriptionValues.getPscName())
                .representativeDetails(descriptionValues.getRepresentativeDetails())
                .withdrawalDate(convertInstantToString(descriptionValues.getWithdrawalDate()))
                .caseStartDate(convertInstantToString(descriptionValues.getCaseStartDate()))
                .resType(descriptionValues.getResType())
                .resolutionDate(convertInstantToString(descriptionValues.getResolutionDate()));
    }

    private static String convertInstantToString(final Instant inputDate) {
        return inputDate == null ? null : LocalDate.ofInstant(inputDate, ZoneOffset.UTC).toString();
    }
}
