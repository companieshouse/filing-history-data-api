package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.InvalidTransactionKindException;

@ExtendWith(MockitoExtension.class)
class ValidatorFactoryTest {

    @InjectMocks
    private ValidatorFactory factory;

    @Mock
    private AnnotationPutRequestValidator annotationPutRequestValidator;

    @Mock
    private TopLevelPutRequestValidator topLevelPutRequestValidator;

    @Test
    void shouldReturnTopLevelValidatorForTopLevelKind() {
        // given

        // when
        Validator<InternalFilingHistoryApi> actual = factory.getPutRequestValidator(InternalData.TransactionKindEnum.TOP_LEVEL);

        // then
        assertInstanceOf(TopLevelPutRequestValidator.class, actual);
        assertEquals(topLevelPutRequestValidator, actual);
    }

    @Test
    void shouldReturnAnnotationValidatorForAnnotationKind() {
        // given

        // when
        Validator<InternalFilingHistoryApi> actual = factory.getPutRequestValidator(InternalData.TransactionKindEnum.ANNOTATION);

        // then
        assertInstanceOf(AnnotationPutRequestValidator.class, actual);
        assertEquals(annotationPutRequestValidator, actual);
    }

    @ParameterizedTest
    @CsvSource({"resolution", "associated-filing"})
    void shouldReturnInvalidMapperExceptionWhenKindDoesNotReturnAMapper(String kind) {
        // given

        // when
        Executable executable = () -> factory.getPutRequestValidator(InternalData.TransactionKindEnum.fromValue(kind));

        // then
        InvalidTransactionKindException exception = assertThrows(InvalidTransactionKindException.class, executable);
        assertEquals("Invalid transaction kind: %s".formatted(kind), exception.getMessage());
    }
}
