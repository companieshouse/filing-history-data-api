package uk.gov.companieshouse.filinghistory.api.model.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryOriginalValues {

    @Field("officer_name")
    @JsonProperty("officer_name")
    private String officerName;

    @Field("resignation_date")
    @JsonProperty("resignation_date")
    private String resignationDate;

    @Field("charge_creation_date")
    @JsonProperty("charge_creation_date")
    private Instant chargeCreationDate;

    @Field("property_acquired_date")
    @JsonProperty("property_acquired_date")
    private Instant propertyAcquiredDate;

    @Field("appointment_date")
    @JsonProperty("appointment_date")
    private String appointmentDate;

    @Field("case_start_date")
    @JsonProperty("case_start_date")
    private String caseStartDate;

    @Field("case_end_date")
    @JsonProperty("case_end_date")
    private String caseEndDate;

    @Field("made_up_date")
    @JsonProperty("made_up_date")
    private String madeUpDate;

    @Field("acc_type")
    @JsonProperty("acc_type")
    private String accType;

    @Field("change_date")
    @JsonProperty("change_date")
    private String changeDate;

    @Field("accounting_period")
    @JsonProperty("accounting_period")
    private String accountingPeriod;

    @Field("period_type")
    @JsonProperty("period_type")
    private String periodType;

    @Field("new_date")
    @JsonProperty("new_date")
    private String newDate;

    @Field("notification_date")
    @JsonProperty("notification_date")
    private String notificationDate;

    @Field("psc_name")
    @JsonProperty("psc_name")
    private String pscName;

    @Field("new_ro_address")
    @JsonProperty("new_ro_address")
    private String newRoAddress;

    @Field("res_type")
    @JsonProperty("res_type")
    private String resType;

    @Field("cessation_date")
    @JsonProperty("cessation_date")
    private String cessationDate;

    @Field("action")
    @JsonProperty("action")
    private String action;

    @Field("capital_type")
    @JsonProperty("capital_type")
    private String capitalType;

    @Field("mortgage_satisfaction_date")
    @JsonProperty("mortgage_satisfaction_date")
    private String mortgageSatisfactionDate;

    public String getOfficerName() {
        return officerName;
    }

    public FilingHistoryOriginalValues officerName(String officerName) {
        this.officerName = officerName;
        return this;
    }

    public String getResignationDate() {
        return resignationDate;
    }

    public FilingHistoryOriginalValues resignationDate(String resignationDate) {
        this.resignationDate = resignationDate;
        return this;
    }

    public Instant getChargeCreationDate() {
        return chargeCreationDate;
    }

    public FilingHistoryOriginalValues chargeCreationDate(Instant chargeCreationDate) {
        this.chargeCreationDate = chargeCreationDate;
        return this;
    }

    public Instant getPropertyAcquiredDate() {
        return propertyAcquiredDate;
    }

    public FilingHistoryOriginalValues propertyAcquiredDate(Instant propertyAcquiredDate) {
        this.propertyAcquiredDate = propertyAcquiredDate;
        return this;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public FilingHistoryOriginalValues appointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
        return this;
    }

    public String getCaseStartDate() {
        return caseStartDate;
    }

    public FilingHistoryOriginalValues caseStartDate(String caseStartDate) {
        this.caseStartDate = caseStartDate;
        return this;
    }

    public String getCaseEndDate() {
        return caseEndDate;
    }

    public FilingHistoryOriginalValues caseEndDate(String caseEndDate) {
        this.caseEndDate = caseEndDate;
        return this;
    }

    public String getMadeUpDate() {
        return madeUpDate;
    }

    public FilingHistoryOriginalValues madeUpDate(String madeUpDate) {
        this.madeUpDate = madeUpDate;
        return this;
    }

    public String getAccType() {
        return accType;
    }

    public FilingHistoryOriginalValues accType(String accType) {
        this.accType = accType;
        return this;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public FilingHistoryOriginalValues changeDate(String changeDate) {
        this.changeDate = changeDate;
        return this;
    }

    public String getAccountingPeriod() {
        return accountingPeriod;
    }

    public FilingHistoryOriginalValues accountingPeriod(String accountingPeriod) {
        this.accountingPeriod = accountingPeriod;
        return this;
    }

    public String getPeriodType() {
        return periodType;
    }

    public FilingHistoryOriginalValues periodType(String periodType) {
        this.periodType = periodType;
        return this;
    }

    public String getNewDate() {
        return newDate;
    }

    public FilingHistoryOriginalValues newDate(String newDate) {
        this.newDate = newDate;
        return this;
    }

    public String getNotificationDate() {
        return notificationDate;
    }

    public FilingHistoryOriginalValues notificationDate(String notificationDate) {
        this.notificationDate = notificationDate;
        return this;
    }

    public String getPscName() {
        return pscName;
    }

    public FilingHistoryOriginalValues pscName(String pscName) {
        this.pscName = pscName;
        return this;
    }

    public String getNewRoAddress() {
        return newRoAddress;
    }

    public FilingHistoryOriginalValues newRoAddress(String newRoAddress) {
        this.newRoAddress = newRoAddress;
        return this;
    }

    public String getResType() {
        return resType;
    }

    public FilingHistoryOriginalValues resType(String resType) {
        this.resType = resType;
        return this;
    }

    public String getCessationDate() {
        return cessationDate;
    }

    public FilingHistoryOriginalValues cessationDate(String cessationDate) {
        this.cessationDate = cessationDate;
        return this;
    }

    public String getAction() {
        return action;
    }

    public FilingHistoryOriginalValues action(String action) {
        this.action = action;
        return this;
    }

    public String getCapitalType() {
        return capitalType;
    }

    public FilingHistoryOriginalValues capitalType(String capitalType) {
        this.capitalType = capitalType;
        return this;
    }

    public String getMortgageSatisfactionDate() {
        return mortgageSatisfactionDate;
    }

    public FilingHistoryOriginalValues mortgageSatisfactionDate(String mortgageSatisfactionDate) {
        this.mortgageSatisfactionDate = mortgageSatisfactionDate;
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
        FilingHistoryOriginalValues that = (FilingHistoryOriginalValues) o;
        return Objects.equals(officerName, that.officerName)
                && Objects.equals(resignationDate, that.resignationDate)
                && Objects.equals(chargeCreationDate, that.chargeCreationDate)
                && Objects.equals(propertyAcquiredDate, that.propertyAcquiredDate)
                && Objects.equals(appointmentDate, that.appointmentDate)
                && Objects.equals(caseStartDate, that.caseStartDate)
                && Objects.equals(caseEndDate, that.caseEndDate)
                && Objects.equals(madeUpDate, that.madeUpDate)
                && Objects.equals(accType, that.accType)
                && Objects.equals(changeDate, that.changeDate)
                && Objects.equals(accountingPeriod, that.accountingPeriod)
                && Objects.equals(periodType, that.periodType)
                && Objects.equals(newDate, that.newDate)
                && Objects.equals(notificationDate, that.notificationDate)
                && Objects.equals(pscName, that.pscName)
                && Objects.equals(newRoAddress, that.newRoAddress)
                && Objects.equals(resType, that.resType)
                && Objects.equals(cessationDate, that.cessationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(officerName, resignationDate, chargeCreationDate, propertyAcquiredDate,
                appointmentDate, caseStartDate, caseEndDate, madeUpDate, accType, changeDate,
                accountingPeriod, periodType, newDate, notificationDate, pscName,
                newRoAddress, resType, cessationDate);
    }

    @Override
    public String toString() {
        return "FilingHistoryOriginalValues{" +
                "officerName='" + officerName + '\'' +
                ", resignationDate='" + resignationDate + '\'' +
                ", chargeCreationDate='" + chargeCreationDate + '\'' +
                ", propertyAcquiredDate='" + propertyAcquiredDate + '\'' +
                ", appointmentDate='" + appointmentDate + '\'' +
                ", caseStartDate='" + caseStartDate + '\'' +
                ", caseEndDate='" + caseEndDate + '\'' +
                ", madeUpDate='" + madeUpDate + '\'' +
                ", accType='" + accType + '\'' +
                ", changeDate='" + changeDate + '\'' +
                ", accountingPeriod='" + accountingPeriod + '\'' +
                ", periodType='" + periodType + '\'' +
                ", newDate='" + newDate + '\'' +
                ", notificationDate='" + notificationDate + '\'' +
                ", pscName='" + pscName + '\'' +
                ", newRoAddress='" + newRoAddress + '\'' +
                ", resType='" + resType + '\'' +
                ", cessationDate='" + cessationDate + '\'' +
                '}';
    }
}
