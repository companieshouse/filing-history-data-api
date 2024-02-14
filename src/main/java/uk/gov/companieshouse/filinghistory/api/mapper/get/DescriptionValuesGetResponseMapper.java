package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.get.DateUtils.convertInstantToLocalDateString;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@Component
public class DescriptionValuesGetResponseMapper {

    public FilingHistoryItemDataDescriptionValues map(FilingHistoryDescriptionValues descriptionValues) {
        return new FilingHistoryItemDataDescriptionValues()
                .appointmentDate(convertInstantToLocalDateString(descriptionValues.getAppointmentDate()))
                .branchNumber(descriptionValues.getBranchNumber())
                .broughtDownDate(convertInstantToLocalDateString(descriptionValues.getBroughtDownDate()))
                .caseEndDate(convertInstantToLocalDateString(descriptionValues.getCaseEndDate()))
                .caseNumber(descriptionValues.getCaseNumber())
                .cessationDate(convertInstantToLocalDateString(descriptionValues.getCessationDate()))
                .changeAddress(descriptionValues.getChangeAddress())
                .changeDate(convertInstantToLocalDateString(descriptionValues.getChangeDate()))
                .changeDetails(descriptionValues.getChangeDetails())
                .changeName(descriptionValues.getChangeName())
                .changeType(descriptionValues.getChangeType())
                .chargeCreationDate(convertInstantToLocalDateString(descriptionValues.getChargeCreationDate()))
                .chargeNumber(descriptionValues.getChargeNumber())
                .closeDate(convertInstantToLocalDateString(descriptionValues.getCloseDate()))
                .companyNumber(descriptionValues.getCompanyNumber())
                .companyType(descriptionValues.getCompanyType())
                .date(convertInstantToLocalDateString(descriptionValues.getDate()))
                .defaultAddress(descriptionValues.getDefaultAddress())
                .description(descriptionValues.getDescription())
                .formAttached(descriptionValues.getFormAttached())
                .formType(descriptionValues.getFormType())
                .incorporationDate(convertInstantToLocalDateString(descriptionValues.getIncorporationDate()))
                .madeUpDate(convertInstantToLocalDateString(descriptionValues.getMadeUpDate()))
                .newAddress(descriptionValues.getNewAddress())
                .newDate(convertInstantToLocalDateString(descriptionValues.getNewDate()))
                .newJurisdiction(descriptionValues.getNewJurisdiction())
                .notificationDate(convertInstantToLocalDateString(descriptionValues.getNotificationDate()))
                .officerAddress(descriptionValues.getOfficerAddress())
                .officerName(descriptionValues.getOfficerName())
                .terminationDate(convertInstantToLocalDateString(descriptionValues.getTerminationDate()))
                .oldAddress(descriptionValues.getOldAddress())
                .oldJurisdiction(descriptionValues.getOldJurisdiction())
                .originalDescription(descriptionValues.getOriginalDescription())
                .propertyAcquiredDate(convertInstantToLocalDateString(descriptionValues.getPropertyAcquiredDate()))
                .pscName(descriptionValues.getPscName())
                .representativeDetails(descriptionValues.getRepresentativeDetails())
                .withdrawalDate(convertInstantToLocalDateString(descriptionValues.getWithdrawalDate()))
                .caseStartDate(convertInstantToLocalDateString(descriptionValues.getCaseStartDate()))
                .resType(descriptionValues.getResType())
                .resolutionDate(convertInstantToLocalDateString(descriptionValues.getResolutionDate()));
    }
}
