package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.instantToString;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;

@Component
public class DescriptionValuesGetResponseMapper {

    private final CapitalDescriptionGetResponseMapper capitalDescriptionMapper;

    public DescriptionValuesGetResponseMapper(CapitalDescriptionGetResponseMapper capitalDescriptionMapper) {
        this.capitalDescriptionMapper = capitalDescriptionMapper;
    }

    public FilingHistoryItemDataDescriptionValues map(FilingHistoryDescriptionValues descriptionValues) {
        return Optional.ofNullable(descriptionValues)
                .map(values -> new FilingHistoryItemDataDescriptionValues()
                        .capital(capitalDescriptionMapper.mapFilingHistoryCapital(values.getCapital()))
                        .altCapital(capitalDescriptionMapper.mapFilingHistoryAltCapital(values.getAltCapital()))
                        .appointmentDate(instantToString(values.getAppointmentDate()))
                        .branchNumber(values.getBranchNumber())
                        .broughtDownDate(instantToString(values.getBroughtDownDate()))
                        .caseEndDate(instantToString(values.getCaseEndDate()))
                        .caseNumber(values.getCaseNumber())
                        .cessationDate(instantToString(values.getCessationDate()))
                        .changeAddress(values.getChangeAddress())
                        .changeDate(instantToString(values.getChangeDate()))
                        .changeDetails(values.getChangeDetails())
                        .changeName(values.getChangeName())
                        .changeType(values.getChangeType())
                        .chargeCreationDate(instantToString(values.getChargeCreationDate()))
                        .chargeNumber(values.getChargeNumber())
                        .closeDate(instantToString(values.getCloseDate()))
                        .companyNumber(values.getCompanyNumber())
                        .companyType(values.getCompanyType())
                        .date(instantToString(values.getDate()))
                        .defaultAddress(values.getDefaultAddress())
                        .description(values.getDescription())
                        .formAttached(values.getFormAttached())
                        .formType(values.getFormType())
                        .incorporationDate(instantToString(values.getIncorporationDate()))
                        .madeUpDate(instantToString(values.getMadeUpDate()))
                        .newAddress(values.getNewAddress())
                        .newDate(instantToString(values.getNewDate()))
                        .newJurisdiction(values.getNewJurisdiction())
                        .notificationDate(instantToString(values.getNotificationDate()))
                        .officerAddress(values.getOfficerAddress())
                        .officerName(values.getOfficerName())
                        .terminationDate(instantToString(values.getTerminationDate()))
                        .oldAddress(values.getOldAddress())
                        .oldJurisdiction(values.getOldJurisdiction())
                        .originalDescription(values.getOriginalDescription())
                        .propertyAcquiredDate(instantToString(values.getPropertyAcquiredDate()))
                        .pscName(values.getPscName())
                        .representativeDetails(values.getRepresentativeDetails())
                        .withdrawalDate(instantToString(values.getWithdrawalDate()))
                        .caseStartDate(instantToString(values.getCaseStartDate()))
                        .resType(values.getResType())
                        .resolutionDate(instantToString(values.getResolutionDate())))
                .orElse(null);
    }
}
