package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

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
    @Mock
    private ResolutionPutRequestValidator resolutionPutRequestValidator;

    @Test
    void shouldReturnTopLevelValidatorForTopLevelKind() {
        // given

        // when
        Validator<InternalFilingHistoryApi> actual = factory.getPutRequestValidator(TransactionKindEnum.TOP_LEVEL);

        // then
        assertInstanceOf(TopLevelPutRequestValidator.class, actual);
        assertEquals(topLevelPutRequestValidator, actual);
    }

    @Test
    void shouldReturnAnnotationValidatorForAnnotationKind() {
        // given

        // when
        Validator<InternalFilingHistoryApi> actual = factory.getPutRequestValidator(TransactionKindEnum.ANNOTATION);

        // then
        assertInstanceOf(AnnotationPutRequestValidator.class, actual);
        assertEquals(annotationPutRequestValidator, actual);
    }

    @Test
    void shouldReturnAssociatedFilingValidatorForAnnotationKind() {
        // given

        // when
        Validator<InternalFilingHistoryApi> actual = factory.getPutRequestValidator(TransactionKindEnum.ASSOCIATED_FILING);

        // then
        assertInstanceOf(AssociatedFilingPutRequestValidator.class, actual);
        assertEquals(associatedFilingPutRequestValidator, actual);
    }

    @Test
    void shouldReturnResolutionValidatorForResolutionKind() {
        // given

        // when
        Validator<InternalFilingHistoryApi> actual = factory.getPutRequestValidator(TransactionKindEnum.RESOLUTION);

        // then
        assertInstanceOf(ResolutionPutRequestValidator.class, actual);
        assertEquals(resolutionPutRequestValidator, actual);
    }
}
