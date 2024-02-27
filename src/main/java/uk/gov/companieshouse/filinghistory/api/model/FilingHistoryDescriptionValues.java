package uk.gov.companieshouse.filinghistory.api.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryDescriptionValues {

    @Field("appointment_date")
    private Instant appointmentDate;
    @Field("branch_number")
    private String branchNumber;
    @Field("brought_down_date")
    private Instant broughtDownDate;
    @Field("case_end_date")
    private Instant caseEndDate;
    @Field("case_number")
    private String caseNumber;
    @Field("cessation_date")
    private Instant cessationDate;
    @Field("change_address")
    private String changeAddress;
    @Field("change_date")
    private Instant changeDate;
    @Field("change_details")
    private String changeDetails;
    @Field("change_name")
    private String changeName;
    @Field("change_type")
    private String changeType;
    @Field("charge_creation_date")
    private Instant chargeCreationDate;
    @Field("charge_number")
    private String chargeNumber;
    @Field("close_date")
    private Instant closeDate;
    @Field("company_number")
    private String companyNumber;
    @Field("company_type")
    private String companyType;
    private Instant date;
    @Field("default_address")
    private String defaultAddress;
    private String description;
    @Field("form_attached")
    private String formAttached;
    @Field("form_type")
    private String formType;
    @Field("incorporation_date")
    private Instant incorporationDate;
    @Field("made_up_date")
    private Instant madeUpDate;
    @Field("new_address")
    private String newAddress;
    @Field("new_date")
    private Instant newDate;
    @Field("new_jurisdiction")
    private String newJurisdiction;
    @Field("notification_date")
    private Instant notificationDate;
    @Field("officer_address")
    private String officerAddress;
    @Field("termination_date")
    private Instant terminationDate;
    @Field("officer_name")
    private String officerName;
    @Field("old_address")
    private String oldAddress;
    @Field("old_jurisdiction")
    private String oldJurisdiction;
    @Field("original_description")
    private String originalDescription;
    @Field("property_acquired_date")
    private Instant propertyAcquiredDate;
    @Field("psc_name")
    private String pscName;
    @Field("representative_details")
    private String representativeDetails;
    @Field("withdrawal_date")
    private Instant withdrawalDate;
    @Field("case_start_date")
    private Instant caseStartDate;
    @Field("res_type")
    private String resType;
    @Field("resolution_date")
    private Instant resolutionDate;
    @Field("capital")
    private List<FilingHistoryCapital> capital;
    @Field("alt_capital")
    private List<FilingHistoryAltCapital> altCapital;

    public Instant getTerminationDate() {
        return terminationDate;
    }

    public FilingHistoryDescriptionValues terminationDate(Instant terminationDate) {
        this.terminationDate = terminationDate;
        return this;
    }

    public String getOfficerName() {
        return officerName;
    }

    public FilingHistoryDescriptionValues officerName(String officerName) {
        this.officerName = officerName;
        return this;
    }

    public Instant getAppointmentDate() {
        return appointmentDate;
    }

    public FilingHistoryDescriptionValues appointmentDate(Instant appointmentDate) {
        this.appointmentDate = appointmentDate;
        return this;
    }

    public String getBranchNumber() {
        return branchNumber;
    }

    public FilingHistoryDescriptionValues branchNumber(String branchNumber) {
        this.branchNumber = branchNumber;
        return this;
    }

    public Instant getBroughtDownDate() {
        return broughtDownDate;
    }

    public FilingHistoryDescriptionValues broughtDownDate(Instant broughtDownDate) {
        this.broughtDownDate = broughtDownDate;
        return this;
    }

    public Instant getCaseEndDate() {
        return caseEndDate;
    }

    public FilingHistoryDescriptionValues caseEndDate(Instant caseEndDate) {
        this.caseEndDate = caseEndDate;
        return this;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public FilingHistoryDescriptionValues caseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
        return this;
    }

    public Instant getCessationDate() {
        return cessationDate;
    }

    public FilingHistoryDescriptionValues cessationDate(Instant cessationDate) {
        this.cessationDate = cessationDate;
        return this;
    }

    public String getChangeAddress() {
        return changeAddress;
    }

    public FilingHistoryDescriptionValues changeAddress(String changeAddress) {
        this.changeAddress = changeAddress;
        return this;
    }

    public Instant getChangeDate() {
        return changeDate;
    }

    public FilingHistoryDescriptionValues changeDate(Instant changeDate) {
        this.changeDate = changeDate;
        return this;
    }

    public String getChangeDetails() {
        return changeDetails;
    }

    public FilingHistoryDescriptionValues changeDetails(String changeDetails) {
        this.changeDetails = changeDetails;
        return this;
    }

    public String getChangeName() {
        return changeName;
    }

    public FilingHistoryDescriptionValues changeName(String changeName) {
        this.changeName = changeName;
        return this;
    }

    public String getChangeType() {
        return changeType;
    }

    public FilingHistoryDescriptionValues changeType(String changeType) {
        this.changeType = changeType;
        return this;
    }

    public Instant getChargeCreationDate() {
        return chargeCreationDate;
    }

    public FilingHistoryDescriptionValues chargeCreationDate(Instant chargeCreationDate) {
        this.chargeCreationDate = chargeCreationDate;
        return this;
    }

    public String getChargeNumber() {
        return chargeNumber;
    }

    public FilingHistoryDescriptionValues chargeNumber(String chargeNumber) {
        this.chargeNumber = chargeNumber;
        return this;
    }

    public Instant getCloseDate() {
        return closeDate;
    }

    public FilingHistoryDescriptionValues closeDate(Instant closeDate) {
        this.closeDate = closeDate;
        return this;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public FilingHistoryDescriptionValues companyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public String getCompanyType() {
        return companyType;
    }

    public FilingHistoryDescriptionValues companyType(String companyType) {
        this.companyType = companyType;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public FilingHistoryDescriptionValues date(Instant date) {
        this.date = date;
        return this;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public FilingHistoryDescriptionValues defaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FilingHistoryDescriptionValues description(String description) {
        this.description = description;
        return this;
    }

    public String getFormAttached() {
        return formAttached;
    }

    public FilingHistoryDescriptionValues formAttached(String formAttached) {
        this.formAttached = formAttached;
        return this;
    }

    public String getFormType() {
        return formType;
    }

    public FilingHistoryDescriptionValues formType(String formType) {
        this.formType = formType;
        return this;
    }

    public Instant getIncorporationDate() {
        return incorporationDate;
    }

    public FilingHistoryDescriptionValues incorporationDate(Instant incorporationDate) {
        this.incorporationDate = incorporationDate;
        return this;
    }

    public Instant getMadeUpDate() {
        return madeUpDate;
    }

    public FilingHistoryDescriptionValues madeUpDate(Instant madeUpDate) {
        this.madeUpDate = madeUpDate;
        return this;
    }

    public String getNewAddress() {
        return newAddress;
    }

    public FilingHistoryDescriptionValues newAddress(String newAddress) {
        this.newAddress = newAddress;
        return this;
    }

    public Instant getNewDate() {
        return newDate;
    }

    public FilingHistoryDescriptionValues newDate(Instant newDate) {
        this.newDate = newDate;
        return this;
    }

    public String getNewJurisdiction() {
        return newJurisdiction;
    }

    public FilingHistoryDescriptionValues newJurisdiction(String newJurisdiction) {
        this.newJurisdiction = newJurisdiction;
        return this;
    }

    public Instant getNotificationDate() {
        return notificationDate;
    }

    public FilingHistoryDescriptionValues notificationDate(Instant notificationDate) {
        this.notificationDate = notificationDate;
        return this;
    }

    public String getOfficerAddress() {
        return officerAddress;
    }

    public FilingHistoryDescriptionValues officerAddress(String officerAddress) {
        this.officerAddress = officerAddress;
        return this;
    }

    public String getOldAddress() {
        return oldAddress;
    }

    public FilingHistoryDescriptionValues oldAddress(String oldAddress) {
        this.oldAddress = oldAddress;
        return this;
    }

    public String getOldJurisdiction() {
        return oldJurisdiction;
    }

    public FilingHistoryDescriptionValues oldJurisdiction(String oldJurisdiction) {
        this.oldJurisdiction = oldJurisdiction;
        return this;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public FilingHistoryDescriptionValues originalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
        return this;
    }

    public Instant getPropertyAcquiredDate() {
        return propertyAcquiredDate;
    }

    public FilingHistoryDescriptionValues propertyAcquiredDate(Instant propertyAcquiredDate) {
        this.propertyAcquiredDate = propertyAcquiredDate;
        return this;
    }

    public String getPscName() {
        return pscName;
    }

    public FilingHistoryDescriptionValues pscName(String pscName) {
        this.pscName = pscName;
        return this;
    }

    public String getRepresentativeDetails() {
        return representativeDetails;
    }

    public FilingHistoryDescriptionValues representativeDetails(String representativeDetails) {
        this.representativeDetails = representativeDetails;
        return this;
    }

    public Instant getWithdrawalDate() {
        return withdrawalDate;
    }

    public FilingHistoryDescriptionValues withdrawalDate(Instant withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
        return this;
    }

    public Instant getCaseStartDate() {
        return caseStartDate;
    }

    public FilingHistoryDescriptionValues caseStartDate(Instant caseStartDate) {
        this.caseStartDate = caseStartDate;
        return this;
    }

    public String getResType() {
        return resType;
    }

    public FilingHistoryDescriptionValues resType(String resType) {
        this.resType = resType;
        return this;
    }

    public Instant getResolutionDate() {
        return resolutionDate;
    }

    public FilingHistoryDescriptionValues resolutionDate(Instant resolutionDate) {
        this.resolutionDate = resolutionDate;
        return this;
    }

    public List<FilingHistoryCapital> getCapital() {
        return capital;
    }

    public FilingHistoryDescriptionValues capital(List<FilingHistoryCapital> capital) {
        this.capital = capital;
        return this;
    }

    public List<FilingHistoryAltCapital> getAltCapital() {
        return altCapital;
    }

    public FilingHistoryDescriptionValues altCapital(List<FilingHistoryAltCapital> altCapital) {
        this.altCapital = altCapital;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilingHistoryDescriptionValues that = (FilingHistoryDescriptionValues) o;
        return Objects.equals(appointmentDate, that.appointmentDate)
                && Objects.equals(branchNumber, that.branchNumber)
                && Objects.equals(broughtDownDate, that.broughtDownDate)
                && Objects.equals(caseEndDate, that.caseEndDate) && Objects.equals(
                caseNumber, that.caseNumber) && Objects.equals(cessationDate,
                that.cessationDate) && Objects.equals(changeAddress, that.changeAddress)
                && Objects.equals(changeDate, that.changeDate) && Objects.equals(
                changeDetails, that.changeDetails) && Objects.equals(changeName,
                that.changeName) && Objects.equals(changeType, that.changeType)
                && Objects.equals(chargeCreationDate, that.chargeCreationDate)
                && Objects.equals(chargeNumber, that.chargeNumber)
                && Objects.equals(closeDate, that.closeDate) && Objects.equals(
                companyNumber, that.companyNumber) && Objects.equals(companyType,
                that.companyType) && Objects.equals(date, that.date)
                && Objects.equals(defaultAddress, that.defaultAddress)
                && Objects.equals(description, that.description) && Objects.equals(
                formAttached, that.formAttached) && Objects.equals(formType, that.formType)
                && Objects.equals(incorporationDate, that.incorporationDate)
                && Objects.equals(madeUpDate, that.madeUpDate) && Objects.equals(
                newAddress, that.newAddress) && Objects.equals(newDate, that.newDate)
                && Objects.equals(newJurisdiction, that.newJurisdiction)
                && Objects.equals(notificationDate, that.notificationDate)
                && Objects.equals(officerAddress, that.officerAddress)
                && Objects.equals(terminationDate, that.terminationDate)
                && Objects.equals(officerName, that.officerName) && Objects.equals(
                oldAddress, that.oldAddress) && Objects.equals(oldJurisdiction,
                that.oldJurisdiction) && Objects.equals(originalDescription,
                that.originalDescription) && Objects.equals(propertyAcquiredDate,
                that.propertyAcquiredDate) && Objects.equals(pscName, that.pscName)
                && Objects.equals(representativeDetails, that.representativeDetails)
                && Objects.equals(withdrawalDate, that.withdrawalDate)
                && Objects.equals(caseStartDate, that.caseStartDate)
                && Objects.equals(resType, that.resType) && Objects.equals(
                resolutionDate, that.resolutionDate) && Objects.equals(capital, that.capital)
                && Objects.equals(altCapital, that.altCapital);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentDate, branchNumber, broughtDownDate, caseEndDate, caseNumber,
                cessationDate, changeAddress, changeDate, changeDetails, changeName, changeType,
                chargeCreationDate, chargeNumber, closeDate, companyNumber, companyType, date,
                defaultAddress, description, formAttached, formType, incorporationDate, madeUpDate,
                newAddress, newDate, newJurisdiction, notificationDate, officerAddress,
                terminationDate,
                officerName, oldAddress, oldJurisdiction, originalDescription, propertyAcquiredDate,
                pscName, representativeDetails, withdrawalDate, caseStartDate, resType,
                resolutionDate,
                capital, altCapital);
    }

    @Override
    public String toString() {
        return "FilingHistoryDescriptionValues{" +
                "appointmentDate=" + appointmentDate +
                ", branchNumber='" + branchNumber + '\'' +
                ", broughtDownDate=" + broughtDownDate +
                ", caseEndDate=" + caseEndDate +
                ", caseNumber='" + caseNumber + '\'' +
                ", cessationDate=" + cessationDate +
                ", changeAddress='" + changeAddress + '\'' +
                ", changeDate=" + changeDate +
                ", changeDetails='" + changeDetails + '\'' +
                ", changeName='" + changeName + '\'' +
                ", changeType='" + changeType + '\'' +
                ", chargeCreationDate=" + chargeCreationDate +
                ", chargeNumber='" + chargeNumber + '\'' +
                ", closeDate=" + closeDate +
                ", companyNumber='" + companyNumber + '\'' +
                ", companyType='" + companyType + '\'' +
                ", date=" + date +
                ", defaultAddress='" + defaultAddress + '\'' +
                ", description='" + description + '\'' +
                ", formAttached='" + formAttached + '\'' +
                ", formType='" + formType + '\'' +
                ", incorporationDate=" + incorporationDate +
                ", madeUpDate=" + madeUpDate +
                ", newAddress='" + newAddress + '\'' +
                ", newDate=" + newDate +
                ", newJurisdiction='" + newJurisdiction + '\'' +
                ", notificationDate=" + notificationDate +
                ", officerAddress='" + officerAddress + '\'' +
                ", terminationDate=" + terminationDate +
                ", officerName='" + officerName + '\'' +
                ", oldAddress='" + oldAddress + '\'' +
                ", oldJurisdiction='" + oldJurisdiction + '\'' +
                ", originalDescription='" + originalDescription + '\'' +
                ", propertyAcquiredDate=" + propertyAcquiredDate +
                ", pscName='" + pscName + '\'' +
                ", representativeDetails='" + representativeDetails + '\'' +
                ", withdrawalDate=" + withdrawalDate +
                ", caseStartDate=" + caseStartDate +
                ", resType='" + resType + '\'' +
                ", resolutionDate=" + resolutionDate +
                ", capital=" + capital +
                ", altCapital=" + altCapital +
                '}';
    }
}
