package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

class TopLevelPutRequestValidatorTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String VALID_SELF_LINK = "/company/12345678/filing-history/transactionId";
    private static final String VALID_DELTA_AT = "20140916230459600643";
    private static final String TM01_TYPE = "TM01";
    private static final String DATE = "2011-08-06T00:00:00.00Z";
    private static final String DESCRIPTION = "description";
    private static final String ENTITY_ID = "entityId";
    private static final String COMPANY_NUMBER = "12345678";

    Validator<InternalFilingHistoryApi> filingHistoryPutRequestValidator = new TopLevelPutRequestValidator();

    @ParameterizedTest
    @MethodSource("scenarios")
    void testValidateReturnsFalse(RequestBodyTestArgument argument) {
        // given

        // when
        final boolean actual = filingHistoryPutRequestValidator.isValid(argument.requestBody());

        // then
        assertFalse(actual);
    }

    @Test
    void testValidReturnsTrue() {
        // given
        InternalFilingHistoryApi validRequestBody = getRequestBody();

        // when
        final boolean actual = filingHistoryPutRequestValidator.isValid(validRequestBody);

        // then
        assertTrue(actual);
    }

    private static Stream<Arguments> scenarios() {
        return Stream.of(
                Arguments.of(
                        Named.of("Null external data object",
                                RequestBodyTestArgument.builder()
                                        .modifyExternalData(null)
                                        .build())),
                Arguments.of(
                        Named.of("Null internal data object",
                                RequestBodyTestArgument.builder()
                                        .modifyInternalData(null)
                                        .build())),
                Arguments.of(
                        Named.of("Null transaction ID",
                                RequestBodyTestArgument.builder()
                                        .modifyTransactionId(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty transaction ID",
                                RequestBodyTestArgument.builder()
                                        .modifyTransactionId("")
                                        .build())),
                Arguments.of(
                        Named.of("Null link object",
                                RequestBodyTestArgument.builder()
                                        .modifyLinks(null)
                                        .build())),
                Arguments.of(
                        Named.of("Null self link",
                                RequestBodyTestArgument.builder()
                                        .modifySelfLink(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty self link",
                                RequestBodyTestArgument.builder()
                                        .modifySelfLink("")
                                        .build())),
                Arguments.of(
                        Named.of("Null type",
                                RequestBodyTestArgument.builder()
                                        .modifyType(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty type",
                                RequestBodyTestArgument.builder()
                                        .modifyType("")
                                        .build())),
                Arguments.of(
                        Named.of("Null date",
                                RequestBodyTestArgument.builder()
                                        .modifyDate(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty date",
                                RequestBodyTestArgument.builder()
                                        .modifyDate("")
                                        .build())),
                Arguments.of(
                        Named.of("Null category",
                                RequestBodyTestArgument.builder()
                                        .modifyCategory(null)
                                        .build())),
                Arguments.of(
                        Named.of("Null description",
                                RequestBodyTestArgument.builder()
                                        .modifyDescription(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty description",
                                RequestBodyTestArgument.builder()
                                        .modifyDescription("")
                                        .build())),
                Arguments.of(
                        Named.of("Null entity ID",
                                RequestBodyTestArgument.builder()
                                        .modifyEntityId(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty entity ID",
                                RequestBodyTestArgument.builder()
                                        .modifyEntityId("")
                                        .build())),
                Arguments.of(
                        Named.of("Null delta at",
                                RequestBodyTestArgument.builder()
                                        .modifyDeltaAt(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty delta at",
                                RequestBodyTestArgument.builder()
                                        .modifyDeltaAt("")
                                        .build())),
                Arguments.of(
                        Named.of("Null company number",
                                RequestBodyTestArgument.builder()
                                        .modifyCompanyNumber(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty company number",
                                RequestBodyTestArgument.builder()
                                        .modifyCompanyNumber("")
                                        .build()))
        );
    }

    private record RequestBodyTestArgument(InternalFilingHistoryApi requestBody) {

        private static RequestBodyTestArgumentBuilder builder() {
            return new RequestBodyTestArgumentBuilder();
        }

        private static class RequestBodyTestArgumentBuilder {

            private final InternalFilingHistoryApi requestBody;

            public RequestBodyTestArgumentBuilder() {
                requestBody = new InternalFilingHistoryApi()
                        .externalData(new ExternalData()
                                .transactionId(TRANSACTION_ID)
                                .type(TM01_TYPE)
                                .date(DATE)
                                .category(CategoryEnum.OFFICERS)
                                .description(DESCRIPTION)
                                .links(new FilingHistoryItemDataLinks()
                                        .self(VALID_SELF_LINK)))
                        .internalData(new InternalData()
                                .entityId(ENTITY_ID)
                                .companyNumber(COMPANY_NUMBER)
                                .deltaAt(VALID_DELTA_AT));
            }

            public RequestBodyTestArgumentBuilder modifyExternalData(final ExternalData value) {
                requestBody.externalData(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyInternalData(final InternalData value) {
                requestBody.internalData(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyTransactionId(final String value) {
                requestBody.getExternalData().transactionId(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyLinks(FilingHistoryItemDataLinks value) {
                requestBody.getExternalData().links(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifySelfLink(final String value) {
                requestBody.getExternalData().getLinks().self(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyType(final String value) {
                requestBody.getExternalData().type(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyDate(final String value) {
                requestBody.getExternalData().date(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyCategory(CategoryEnum value) {
                requestBody.getExternalData().category(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyDescription(final String value) {
                requestBody.getExternalData().description(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyEntityId(final String value) {
                requestBody.getInternalData().entityId(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyDeltaAt(final String value) {
                requestBody.getInternalData().deltaAt(value);
                return this;
            }

            public RequestBodyTestArgumentBuilder modifyCompanyNumber(final String value) {
                requestBody.getInternalData().companyNumber(value);
                return this;
            }

            public RequestBodyTestArgument build() {
                return new RequestBodyTestArgument(this.requestBody);
            }
        }
    }

    private static InternalFilingHistoryApi getRequestBody() {
        return new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .type(TM01_TYPE)
                        .date(DATE)
                        .category(CategoryEnum.OFFICERS)
                        .description(DESCRIPTION)
                        .links(new FilingHistoryItemDataLinks()
                                .self(VALID_SELF_LINK)))
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .deltaAt(VALID_DELTA_AT));
    }
}
