package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Links;

class AssociatedFilingPutRequestValidatorTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String VALID_SELF_LINK = "/company/12345678/filing-history/transactionId";
    private static final String VALID_DELTA_AT = "20140916230459600643";
    private static final String TYPE = "363(288)";
    private static final String DATE = "2011-08-06T00:00:00.00Z";
    private static final String ENTITY_ID = "entityId";
    private static final String COMPANY_NUMBER = "12345678";

    Validator<InternalFilingHistoryApi> filingHistoryPutRequestValidator = new AssociatedFilingPutRequestValidator();

    @ParameterizedTest
    @MethodSource("scenarios")
    void testValidateReturnsFalseWhenExternalAndInternalDataAreBothNull(RequestBodyTestArgument argument) {
        // given

        // when
        final boolean actual = filingHistoryPutRequestValidator.isValid(argument.requestBody());

        // then
        assertFalse(actual);
    }

    @Test
    void testValidRequestBodyReturnsTrue() {
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
                        Named.of("Null associated filings list",
                                RequestBodyTestArgument.builder()
                                        .modifyAssociatedFilingList(null)
                                        .build())),
                Arguments.of(
                        Named.of("Empty associated filings list",
                                RequestBodyTestArgument.builder()
                                        .modifyAssociatedFilingList(Collections.emptyList())
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

        private static RequestBodyTestArgument.RequestBodyTestArgumentBuilder builder() {
            return new RequestBodyTestArgument.RequestBodyTestArgumentBuilder();
        }

        private static class RequestBodyTestArgumentBuilder {

            private final InternalFilingHistoryApi requestBody;

            public RequestBodyTestArgumentBuilder() {
                requestBody = new InternalFilingHistoryApi()
                        .externalData(new ExternalData()
                                .transactionId(TRANSACTION_ID)
                                .associatedFilings(List.of(
                                        new AssociatedFiling()
                                                .type(TYPE)
                                                .date(DATE)
                                ))
                                .links(new Links()
                                        .self(VALID_SELF_LINK)))
                        .internalData(new InternalData()
                                .entityId(ENTITY_ID)
                                .companyNumber(COMPANY_NUMBER)
                                .deltaAt(VALID_DELTA_AT));
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyExternalData(final ExternalData value) {
                requestBody.externalData(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyInternalData(final InternalData value) {
                requestBody.internalData(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyTransactionId(final String value) {
                requestBody.getExternalData().transactionId(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyLinks(Links value) {
                requestBody.getExternalData().links(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifySelfLink(final String value) {
                requestBody.getExternalData().getLinks().self(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyAssociatedFilingList(List<AssociatedFiling> list) {
                requestBody.getExternalData().associatedFilings(list);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyType(final String value) {
                requestBody.getExternalData().getAssociatedFilings().getFirst().type(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyDate(final String value) {
                requestBody.getExternalData().getAssociatedFilings().getFirst().date(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyEntityId(final String value) {
                requestBody.getInternalData().entityId(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyDeltaAt(final String value) {
                requestBody.getInternalData().deltaAt(value);
                return this;
            }

            public RequestBodyTestArgument.RequestBodyTestArgumentBuilder modifyCompanyNumber(final String value) {
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
                        .associatedFilings(List.of(
                                        new AssociatedFiling()
                                                .type(TYPE)
                                                .date(DATE)
                                )
                        )
                        .links(new Links()
                                .self(VALID_SELF_LINK)))
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .deltaAt(VALID_DELTA_AT));
    }
}
