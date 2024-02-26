package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;

class OriginalValuesMapperTest {

    private static final String OFFICER_NAME = "John Tester";
    private static final String RESIGNATION_DATE = "06/08/2011";
    private static final String CHARGE_CREATION_DATE = "2011-06-01T00:00:00.00Z";
    private static final Instant CHARGE_CREATION_DATE_INSTANT = Instant.parse(CHARGE_CREATION_DATE);
    private static final String PROPERTY_ACQUIRED_DATE = "2011-06-02T00:00:00.00Z";
    private static final Instant PROPERTY_ACQUIRED_DATE_INSTANT = Instant.parse(PROPERTY_ACQUIRED_DATE);
    private static final String APPOINTMENT_DATE = "03/06/2011";
    private static final String CASE_START_DATE = "04/06/2011";
    private static final String CASE_END_DATE = "05/06/2011";
    private static final String MADE_UP_DATE = "06/06/2011";
    private static final String CHANGE_DATE = "07/06/2011";
    private static final String NEW_DATE = "08/06/2011";
    private static final String NOTIFICATION_DATE = "09/06/2011";
    private static final String CESSATION_DATE = "01/07/2011";
    private static final String MORTGAGE_SATISFACTION_DATE = "01/07/2011";
    private static final String ACC_TYPE = "FULL";
    private static final String ACCOUNTING_PERIOD = "accounting period";
    private static final String PERIOD_TYPE = "period type";
    private static final String EXTENDED = "extended";
    private static final String PSC_NAME = "psc name";
    private static final String NEW_RO_ADDRESS = "new ro address";
    private static final String RES_TYPE = "ADOPT ARTICLES";
    private static final String ACTION = "action";
    private static final String CAPITAL_TYPE = "capital type";
    private final OriginalValuesMapper mapper = new OriginalValuesMapper();

    @Test
    void shouldMapOriginalValues() {
        // given
        InternalDataOriginalValues originalValues = new InternalDataOriginalValues()
                .resignationDate(RESIGNATION_DATE)
                .chargeCreationDate(CHARGE_CREATION_DATE)
                .propertyAcquiredDate(PROPERTY_ACQUIRED_DATE)
                .appointmentDate(APPOINTMENT_DATE)
                .caseStartDate(CASE_START_DATE)
                .caseEndDate(CASE_END_DATE)
                .madeUpDate(MADE_UP_DATE)
                .accType(ACC_TYPE)
                .changeDate(CHANGE_DATE)
                .officerName(OFFICER_NAME)
                .accountingPeriod(ACCOUNTING_PERIOD)
                .periodType(PERIOD_TYPE)
                .extended(EXTENDED)
                .newDate(NEW_DATE)
                .notificationDate(NOTIFICATION_DATE)
                .pscName(PSC_NAME)
                .newRoAddress(NEW_RO_ADDRESS)
                .resType(RES_TYPE)
                .cessationDate(CESSATION_DATE)
                .action(ACTION)
                .capitalType(CAPITAL_TYPE)
                .mortgageSatisfactionDate(MORTGAGE_SATISFACTION_DATE);

        FilingHistoryOriginalValues expected = new FilingHistoryOriginalValues()
                .resignationDate(RESIGNATION_DATE)
                .chargeCreationDate(CHARGE_CREATION_DATE_INSTANT)
                .propertyAcquiredDate(PROPERTY_ACQUIRED_DATE_INSTANT)
                .appointmentDate(APPOINTMENT_DATE)
                .caseStartDate(CASE_START_DATE)
                .caseEndDate(CASE_END_DATE)
                .madeUpDate(MADE_UP_DATE)
                .accType(ACC_TYPE)
                .changeDate(CHANGE_DATE)
                .officerName(OFFICER_NAME)
                .accountingPeriod(ACCOUNTING_PERIOD)
                .periodType(PERIOD_TYPE)
                .extended(EXTENDED)
                .newDate(NEW_DATE)
                .notificationDate(NOTIFICATION_DATE)
                .pscName(PSC_NAME)
                .newRoAddress(NEW_RO_ADDRESS)
                .resType(RES_TYPE)
                .cessationDate(CESSATION_DATE)
                .action(ACTION)
                .capitalType(CAPITAL_TYPE)
                .mortgageSatisfactionDate(MORTGAGE_SATISFACTION_DATE);

        // when
        FilingHistoryOriginalValues actual = mapper.map(originalValues);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNullWhenOriginalValuesNull() {
        // given

        // when
        FilingHistoryOriginalValues actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}