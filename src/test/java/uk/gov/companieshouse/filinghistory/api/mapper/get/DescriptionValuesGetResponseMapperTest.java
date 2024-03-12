package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
class DescriptionValuesGetResponseMapperTest {

    private static final String TIME_AND_ZONE = "T00:00:00Z";
    private static final String APPOINTMENT_DATE = "2010-01-01";
    private static final Instant APPOINTMENT_DATE_INSTANT = Instant.parse(APPOINTMENT_DATE + TIME_AND_ZONE);
    private static final String BROUGHT_DOWN_DATE = "2011-02-02";
    private static final Instant BROUGHT_DOWN_DATE_INSTANT = Instant.parse(BROUGHT_DOWN_DATE + TIME_AND_ZONE);
    private static final String CASE_END_DATE = "2013-05-06";
    private static final Instant CASE_END_DATE_INSTANT = Instant.parse(CASE_END_DATE + TIME_AND_ZONE);
    private static final String CESSATION_DATE = "2013-06-05";
    private static final Instant CESSATION_DATE_INSTANT = Instant.parse(CESSATION_DATE + TIME_AND_ZONE);
    private static final String CHANGE_DATE = "2013-04-04";
    private static final Instant CHANGE_DATE_INSTANT = Instant.parse(CHANGE_DATE + TIME_AND_ZONE);
    private static final String CHARGE_CREATION_DATE = "2014-05-05";
    private static final Instant CHARGE_CREATION_DATE_INSTANT = Instant.parse(CHARGE_CREATION_DATE + TIME_AND_ZONE);
    private static final String CLOSE_DATE = "2015-06-06";
    private static final Instant CLOSE_DATE_INSTANT = Instant.parse(CLOSE_DATE + TIME_AND_ZONE);
    private static final String DATE = "2016-07-07";
    private static final Instant DATE_INSTANT = Instant.parse(DATE + TIME_AND_ZONE);
    private static final String INCORPORATION_DATE = "2017-08-08";
    private static final Instant INCORPORATION_DATE_INSTANT = Instant.parse(INCORPORATION_DATE + TIME_AND_ZONE);
    private static final String MADE_UP_DATE = "2018-09-09";
    private static final Instant MADE_UP_DATE_INSTANT = Instant.parse(MADE_UP_DATE + TIME_AND_ZONE);
    private static final String NEW_DATE = "2019-10-10";
    private static final Instant NEW_DATE_INSTANT = Instant.parse(NEW_DATE + TIME_AND_ZONE);
    private static final String NOTIFICATION_DATE = "2020-11-11";
    private static final Instant NOTIFICATION_DATE_INSTANT = Instant.parse(NOTIFICATION_DATE + TIME_AND_ZONE);
    private static final String PROPERTY_ACQUIRED_DATE = "2021-12-12";
    private static final Instant PROPERTY_ACQUIRED_DATE_INSTANT = Instant.parse(PROPERTY_ACQUIRED_DATE + TIME_AND_ZONE);
    private static final String TERMINATION_DATE = "2022-01-31";
    private static final Instant TERMINATION_DATE_INSTANT = Instant.parse(TERMINATION_DATE + TIME_AND_ZONE);
    private static final String WITHDRAWAL_DATE = "2023-02-01";
    private static final Instant WITHDRAWAL_DATE_INSTANT = Instant.parse(WITHDRAWAL_DATE + TIME_AND_ZONE);

    @InjectMocks
    private DescriptionValuesGetResponseMapper mapper;
    @Mock
    private CapitalDescriptionGetResponseMapper capitalDescriptionGetResponseMapper;
    @Mock
    private FilingHistoryCapital filingHistoryCapital;
    @Mock
    private CapitalDescriptionValue capitalDescriptionValue;
    @Mock
    private FilingHistoryAltCapital filingHistoryAltCapital;
    @Mock
    private AltCapitalDescriptionValue altCapitalDescriptionValue;

    @Test
    void shouldSuccessfullyMapDescriptionValues() {
        // given
        final FilingHistoryItemDataDescriptionValues expected = buildExpectedResponseDescriptionValues();

        when(capitalDescriptionGetResponseMapper.mapFilingHistoryCapital(any())).thenReturn(
                List.of(capitalDescriptionValue));
        when(capitalDescriptionGetResponseMapper.mapFilingHistoryAltCapital(any())).thenReturn(
                List.of(altCapitalDescriptionValue));

        // when
        final FilingHistoryItemDataDescriptionValues actual = mapper.map(buildDocumentDescriptionValues());

        // then
        assertEquals(expected, actual);
        verify(capitalDescriptionGetResponseMapper).mapFilingHistoryCapital(List.of(filingHistoryCapital));
        verify(capitalDescriptionGetResponseMapper).mapFilingHistoryAltCapital(List.of(filingHistoryAltCapital));
    }

    @Test
    void shouldReturnNullWhenPassedNullDescriptionValues() {
        // given

        // when
        final FilingHistoryItemDataDescriptionValues actual = mapper.map(null);

        // then
        assertNull(actual);
    }

    private FilingHistoryDescriptionValues buildDocumentDescriptionValues() {
        return new FilingHistoryDescriptionValues()
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
                .withdrawalDate(WITHDRAWAL_DATE_INSTANT);
    }

    private FilingHistoryItemDataDescriptionValues buildExpectedResponseDescriptionValues() {
        return new FilingHistoryItemDataDescriptionValues()
                .altCapital(List.of(altCapitalDescriptionValue))
                .appointmentDate(APPOINTMENT_DATE)
                .branchNumber("50")
                .broughtDownDate(BROUGHT_DOWN_DATE)
                .capital(List.of(capitalDescriptionValue))
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
                .withdrawalDate(WITHDRAWAL_DATE);
    }
}
