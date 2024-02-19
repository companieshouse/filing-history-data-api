package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

class FilingHistoryRequestValidatorTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String VALID_SELF_LINK = "/company/12345678/filing-history/transactionId";
    private static final String VALID_DELTA_AT = "20140916230459600643";
    private static final String TM01_TYPE = "TM01";
    private static final String DATE = "2011-08-06T00:00:00.00Z";
    private static final String DESCRIPTION = "description";
    private static final String ENTITY_ID = "entityId";

    Validator<InternalFilingHistoryApi> filingHistoryRequestValidator = new FilingHistoryRequestValidator<>();

    @ParameterizedTest
    @MethodSource("badRequestScenarios")
    void testValidateThrowsBadRequestExceptionWhenExternalAndInternalDataAreBothNull(InternalFilingHistoryApi requestBody) {
        // given

        // when
        final ServiceResult actual = filingHistoryRequestValidator.validate(requestBody);

        // then
        assertEquals(ServiceResult.BAD_REQUEST, actual);
    }

    @Test
    void testValidRequestBodyDoesNotThrowException() {
        // given
        InternalFilingHistoryApi validRequestBody = new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .type(TM01_TYPE)
                        .date(DATE)
                        .category(ExternalData.CategoryEnum.OFFICERS)
                        .description(DESCRIPTION)
                        .links(new FilingHistoryItemDataLinks()
                                .self(VALID_SELF_LINK)))
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(VALID_DELTA_AT));

        // when
        final ServiceResult actual = filingHistoryRequestValidator.validate(validRequestBody);

        // then
        assertEquals(ServiceResult.VALID_REQUEST, actual);
    }

    private static Stream<Arguments> badRequestScenarios() {
        return Stream.of(
                Arguments.of(
                        Named.of("Null external and internal data objects",
                                new InternalFilingHistoryApi())),
                Arguments.of(
                        Named.of("Null external data object",
                                new InternalFilingHistoryApi()
                                        .internalData(new InternalData()))),
                Arguments.of(
                        Named.of("Null internal data object",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()))),
                Arguments.of(
                        Named.of("Null transaction ID",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .type(TM01_TYPE)
                                                .date(DATE)
                                                .category(ExternalData.CategoryEnum.OFFICERS)
                                                .description(DESCRIPTION)
                                                .links(new FilingHistoryItemDataLinks()
                                                        .self(VALID_SELF_LINK)))
                                        .internalData(new InternalData()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(VALID_DELTA_AT)))),
                Arguments.of(
                        Named.of("Empty transaction ID",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .transactionId("")
                                                .type(TM01_TYPE)
                                                .date(DATE)
                                                .category(ExternalData.CategoryEnum.OFFICERS)
                                                .description(DESCRIPTION)
                                                .links(new FilingHistoryItemDataLinks()
                                                        .self(VALID_SELF_LINK)))
                                        .internalData(new InternalData()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(VALID_DELTA_AT)))),
                Arguments.of(
                        Named.of("Null self link",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .transactionId(TRANSACTION_ID)
                                                .type(TM01_TYPE)
                                                .date(DATE)
                                                .category(ExternalData.CategoryEnum.OFFICERS)
                                                .description(DESCRIPTION))
                                        .internalData(new InternalData()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(VALID_DELTA_AT)))),
                Arguments.of(
                        Named.of("Empty type",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .transactionId(TRANSACTION_ID)
                                                .type("")
                                                .date(DATE)
                                                .category(ExternalData.CategoryEnum.OFFICERS)
                                                .description(DESCRIPTION)
                                                .links(new FilingHistoryItemDataLinks()
                                                        .self(VALID_SELF_LINK)))
                                        .internalData(new InternalData()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(VALID_DELTA_AT)))),
                Arguments.of(
                        Named.of("Empty date",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .transactionId(TRANSACTION_ID)
                                                .type(TM01_TYPE)
                                                .date("")
                                                .category(ExternalData.CategoryEnum.OFFICERS)
                                                .description(DESCRIPTION)
                                                .links(new FilingHistoryItemDataLinks()
                                                        .self(VALID_SELF_LINK)))
                                        .internalData(new InternalData()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(VALID_DELTA_AT)))),
                Arguments.of(
                        Named.of("Null category",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .transactionId(TRANSACTION_ID)
                                                .type(TM01_TYPE)
                                                .date(DATE)
                                                .description(DESCRIPTION)
                                                .links(new FilingHistoryItemDataLinks()
                                                        .self(VALID_SELF_LINK)))
                                        .internalData(new InternalData()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(VALID_DELTA_AT)))),
                Arguments.of(
                        Named.of("Empty description",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .transactionId(TRANSACTION_ID)
                                                .type(TM01_TYPE)
                                                .date(DATE)
                                                .category(ExternalData.CategoryEnum.OFFICERS)
                                                .description("")
                                                .links(new FilingHistoryItemDataLinks()
                                                        .self(VALID_SELF_LINK)))
                                        .internalData(new InternalData()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(VALID_DELTA_AT)))),
                Arguments.of(
                        Named.of("Empty entity ID",
                                new InternalFilingHistoryApi()
                                        .externalData(new ExternalData()
                                                .transactionId(TRANSACTION_ID)
                                                .type(TM01_TYPE)
                                                .date(DATE)
                                                .category(ExternalData.CategoryEnum.OFFICERS)
                                                .description(DESCRIPTION)
                                                .links(new FilingHistoryItemDataLinks()
                                                        .self(VALID_SELF_LINK)))
                                        .internalData(new InternalData()
                                                .entityId("")
                                                .deltaAt(VALID_DELTA_AT))))
        );
    }
}
