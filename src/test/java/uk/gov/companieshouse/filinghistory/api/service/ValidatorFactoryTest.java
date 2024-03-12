package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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
    private AssociatedFilingPutRequestValidator associatedFilingPutRequestValidator;
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

    @Test
    void shouldReturnAssociatedFilingValidatorForAnnotationKind() {
        // given

        // when
        Validator<InternalFilingHistoryApi> actual = factory.getPutRequestValidator(InternalData.TransactionKindEnum.ASSOCIATED_FILING);

        // then
        assertInstanceOf(AssociatedFilingPutRequestValidator.class, actual);
        assertEquals(associatedFilingPutRequestValidator, actual);
    }

    // REMOVE TEST WHEN RESOLUTIONS ARE IMPLEMENTED
    @Test
    void shouldReturnInvalidMapperExceptionWhenKindDoesNotReturnAMapper() {
        // given

        // when
        Executable executable = () -> factory.getPutRequestValidator(InternalData.TransactionKindEnum.fromValue("resolution"));

        // then
        InvalidTransactionKindException exception = assertThrows(InvalidTransactionKindException.class, executable);
        assertEquals("Invalid transaction kind: %s".formatted("resolution"), exception.getMessage());
    }
}
