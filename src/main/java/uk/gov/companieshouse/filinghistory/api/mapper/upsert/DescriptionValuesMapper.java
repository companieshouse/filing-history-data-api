package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@Component
public class DescriptionValuesMapper {

    private final CapitalDescriptionMapper capitalDescriptionMapper;

    public DescriptionValuesMapper(CapitalDescriptionMapper capitalDescriptionMapper) {
        this.capitalDescriptionMapper = capitalDescriptionMapper;
    }

    FilingHistoryDescriptionValues map(final FilingHistoryItemDataDescriptionValues descriptionValues) {
        return Optional.ofNullable(descriptionValues)
                .map(values -> new FilingHistoryDescriptionValues()
                        .capital(capitalDescriptionMapper.mapCapitalDescriptionValueList(values.getCapital()))
                        .altCapital(capitalDescriptionMapper.mapAltCapitalDescriptionValueList(values.getAltCapital()))
                        .terminationDate(stringToInstant(values.getTerminationDate()))
                        .officerName(values.getOfficerName())
                        .appointmentDate(stringToInstant(values.getAppointmentDate()))
                        .branchNumber(values.getBranchNumber())
                        .broughtDownDate(stringToInstant(values.getBroughtDownDate()))
                        .caseEndDate(stringToInstant(values.getCaseEndDate()))
                        .caseNumber(values.getCaseNumber())
                        .cessationDate(stringToInstant(values.getCessationDate()))
                        .changeAddress(values.getChangeAddress())
                        .changeDate(stringToInstant(values.getChangeDate()))
                        .changeDetails(values.getChangeDetails())
                        .changeName(values.getChangeName())
                        .changeType(values.getChangeType())
                        .chargeCreationDate(stringToInstant(values.getChargeCreationDate()))
                        .chargeNumber(values.getChargeNumber())
                        .closeDate(stringToInstant(values.getCloseDate()))
                        .companyNumber(values.getCompanyNumber())
                        .companyType(values.getCompanyType())
                        .date(stringToInstant(values.getDate()))
                        .defaultAddress(values.getDefaultAddress())
                        .description(values.getDescription())
                        .formAttached(values.getFormAttached())
                        .formType(values.getFormType())
                        .incorporationDate(stringToInstant(values.getIncorporationDate()))
                        .madeUpDate(stringToInstant(values.getMadeUpDate()))
                        .newAddress(values.getNewAddress())
                        .newDate(stringToInstant(values.getNewDate()))
                        .newJurisdiction(values.getNewJurisdiction())
                        .notificationDate(stringToInstant(values.getNotificationDate()))
                        .officerAddress(values.getOfficerAddress())
                        .oldAddress(values.getOldAddress())
                        .oldJurisdiction(values.getOldJurisdiction())
                        .originalDescription(values.getOriginalDescription())
                        .propertyAcquiredDate(stringToInstant(values.getPropertyAcquiredDate()))
                        .pscName(values.getPscName())
                        .representativeDetails(values.getRepresentativeDetails())
                        .withdrawalDate(stringToInstant(values.getWithdrawalDate()))
                        .caseStartDate(stringToInstant(values.getCaseStartDate()))
                        .resType(values.getResType())
                        .resolutionDate(stringToInstant(values.getResolutionDate())))
                .orElse(null);
    }
}
