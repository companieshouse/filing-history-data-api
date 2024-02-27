package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAltCapital;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryCapital;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class DescriptionValuesMapperTest {

    private static final String TERMINATION_DATE = "2022-01-31T00:00:00.00Z";
    private static final String APPOINTMENT_DATE = "2022-01-01T00:00:00.00Z";
    private static final String BROUGHT_DOWN_DATE = "2022-01-02T00:00:00.00Z";
    private static final String CASE_END_DATE = "2022-01-03T00:00:00.00Z";
    private static final String CESSATION_DATE = "2022-01-04T00:00:00.00Z";
    private static final String CHANGE_DATE = "2022-01-05T00:00:00.00Z";
    private static final String CHARGE_CREATION_DATE = "2022-01-06T00:00:00.00Z";
    private static final String CLOSE_DATE = "2022-01-07T00:00:00.00Z";
    private static final String DATE = "2022-01-08T00:00:00.00Z";
    private static final String INCORPORATION_DATE = "2022-01-09T00:00:00.00Z";
    private static final String MADE_UP_DATE = "2022-01-10T00:00:00.00Z";
    private static final String NEW_DATE = "2022-01-11T00:00:00.00Z";
    private static final String NOTIFICATION_DATE = "2022-01-12T00:00:00.00Z";
    private static final String PROPERTY_ACQUIRED_DATE = "2022-01-13T00:00:00.00Z";
    private static final String WITHDRAWAL_DATE = "2022-01-14T00:00:00.00Z";
    private static final String CASE_START_DATE = "2022-01-15T00:00:00.00Z";
    private static final String RESOLUTION_DATE = "2022-01-16T00:00:00.00Z";

    private static final Instant TERMINATION_DATE_INSTANT = Instant.parse(TERMINATION_DATE);
    private static final Instant APPOINTMENT_DATE_INSTANT = Instant.parse(APPOINTMENT_DATE);
    private static final Instant BROUGHT_DOWN_DATE_INSTANT = Instant.parse(BROUGHT_DOWN_DATE);
    private static final Instant CASE_END_DATE_INSTANT = Instant.parse(CASE_END_DATE);
    private static final Instant CESSATION_DATE_INSTANT = Instant.parse(CESSATION_DATE);
    private static final Instant CHANGE_DATE_INSTANT = Instant.parse(CHANGE_DATE);
    private static final Instant CHARGE_CREATION_DATE_INSTANT = Instant.parse(CHARGE_CREATION_DATE);
    private static final Instant CLOSE_DATE_INSTANT = Instant.parse(CLOSE_DATE);
    private static final Instant DATE_INSTANT = Instant.parse(DATE);
    private static final Instant INCORPORATION_DATE_INSTANT = Instant.parse(INCORPORATION_DATE);
    private static final Instant MADE_UP_DATE_INSTANT = Instant.parse(MADE_UP_DATE);
    private static final Instant NEW_DATE_INSTANT = Instant.parse(NEW_DATE);
    private static final Instant NOTIFICATION_DATE_INSTANT = Instant.parse(NOTIFICATION_DATE);
    private static final Instant PROPERTY_ACQUIRED_DATE_INSTANT = Instant.parse(PROPERTY_ACQUIRED_DATE);
    private static final Instant WITHDRAWAL_DATE_INSTANT = Instant.parse(WITHDRAWAL_DATE);
    private static final Instant CASE_START_DATE_INSTANT = Instant.parse(CASE_START_DATE);
    private static final Instant RESOLUTION_DATE_INSTANT = Instant.parse(RESOLUTION_DATE);

    @Mock
    private CapitalDescriptionMapper capitalDescriptionMapper;
    @Mock
    private FilingHistoryCapital filingHistoryCapital;
    @Mock
    private FilingHistoryAltCapital filingHistoryAltCapital;

    @InjectMocks
    private DescriptionValuesMapper mapper;

    @Test
    void shouldMapDescriptionValues() {
        // given
        when(capitalDescriptionMapper.mapCapitalDescriptionValueList(anyList())).thenReturn(
                List.of(filingHistoryCapital));
        when(capitalDescriptionMapper.mapAltCapitalDescriptionValueList(anyList())).thenReturn(
                List.of(filingHistoryAltCapital));

        FilingHistoryItemDataDescriptionValues descriptionValues = new FilingHistoryItemDataDescriptionValues()
                .altCapital(List.of(new AltCapitalDescriptionValue()))
                .appointmentDate(APPOINTMENT_DATE)
                .branchNumber("50")
                .broughtDownDate(BROUGHT_DOWN_DATE)
                .capital(List.of(new CapitalDescriptionValue()))
                .caseEndDate(CASE_END_DATE)
                .caseNumber("123")
                .cessationDate(CESSATION_DATE)
                .changeAddress("11 Test Lane")
                .changeDate(CHANGE_DATE)
                .changeDetails("5 Test St")
                .changeName("John Tester")
                .changeType("type")
                .chargeCreationDate(CHARGE_CREATION_DATE)
                .chargeNumber("1")
                .closeDate(CLOSE_DATE)
                .companyNumber("12345678")
                .companyType("LLP")
                .date(DATE)
                .defaultAddress("5 Default Road")
                .description("description")
                .formAttached("attached form")
                .formType("TM01")
                .incorporationDate(INCORPORATION_DATE)
                .madeUpDate(MADE_UP_DATE)
                .newAddress("6 New town Crescent")
                .newDate(NEW_DATE)
                .newJurisdiction("Cardiff")
                .notificationDate(NOTIFICATION_DATE)
                .officerAddress("201 Officer Drive")
                .officerName("John Doe")
                .oldAddress("5 Old Kent Road")
                .oldJurisdiction("London")
                .originalDescription("original")
                .propertyAcquiredDate(PROPERTY_ACQUIRED_DATE)
                .pscName("Significant Person")
                .representativeDetails("details representing")
                .terminationDate(TERMINATION_DATE)
                .withdrawalDate(WITHDRAWAL_DATE)
                .caseStartDate(CASE_START_DATE)
                .resType("res type")
                .resolutionDate(RESOLUTION_DATE);

        FilingHistoryDescriptionValues expected = new FilingHistoryDescriptionValues()
                .altCapital(List.of(filingHistoryAltCapital))
                .appointmentDate(APPOINTMENT_DATE_INSTANT)
                .branchNumber("50")
                .broughtDownDate(BROUGHT_DOWN_DATE_INSTANT)
                .capital(List.of(filingHistoryCapital))
                .caseEndDate(CASE_END_DATE_INSTANT)
                .caseNumber("123")
                .cessationDate(CESSATION_DATE_INSTANT)
                .changeAddress("11 Test Lane")
                .changeDate(CHANGE_DATE_INSTANT)
                .changeDetails("5 Test St")
                .changeName("John Tester")
                .changeType("type")
                .chargeCreationDate(CHARGE_CREATION_DATE_INSTANT)
                .chargeNumber("1")
                .closeDate(CLOSE_DATE_INSTANT)
                .companyNumber("12345678")
                .companyType("LLP")
                .date(DATE_INSTANT)
                .defaultAddress("5 Default Road")
                .description("description")
                .formAttached("attached form")
                .formType("TM01")
                .incorporationDate(INCORPORATION_DATE_INSTANT)
                .madeUpDate(MADE_UP_DATE_INSTANT)
                .newAddress("6 New town Crescent")
                .newDate(NEW_DATE_INSTANT)
                .newJurisdiction("Cardiff")
                .notificationDate(NOTIFICATION_DATE_INSTANT)
                .officerAddress("201 Officer Drive")
                .officerName("John Doe")
                .oldAddress("5 Old Kent Road")
                .oldJurisdiction("London")
                .originalDescription("original")
                .propertyAcquiredDate(PROPERTY_ACQUIRED_DATE_INSTANT)
                .pscName("Significant Person")
                .representativeDetails("details representing")
                .terminationDate(TERMINATION_DATE_INSTANT)
                .withdrawalDate(WITHDRAWAL_DATE_INSTANT)
                .caseStartDate(CASE_START_DATE_INSTANT)
                .resType("res type")
                .resolutionDate(RESOLUTION_DATE_INSTANT);

        // when
        FilingHistoryDescriptionValues actual = mapper.map(descriptionValues);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNullWhenDescriptionValuesNull() {
        // given

        // when
        FilingHistoryDescriptionValues actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}