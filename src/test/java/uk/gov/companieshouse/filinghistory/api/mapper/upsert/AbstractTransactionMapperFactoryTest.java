package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.ANNOTATION;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.ASSOCIATED_FILING;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.RESOLUTION;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractTransactionMapperFactoryTest {

    @InjectMocks
    private AbstractTransactionMapperFactory factory;

    @Mock
    private TopLevelTransactionMapper topLevelTransactionMapper;
    @Mock
    private AnnotationTransactionMapper annotationTransactionMapper;
    @Mock
    private AssociatedFilingTransactionMapper associatedFilingTransactionMapper;
    @Mock
    private ResolutionTransactionMapper resolutionTransactionMapper;

    @Test
    void shouldReturnTopLevelTransactionMapperWhenTopLevelKindPassed() {
        // given

        // when
        AbstractTransactionMapper actualMapper = factory.getTransactionMapper(TOP_LEVEL);

        // then
        assertInstanceOf(TopLevelTransactionMapper.class, actualMapper);
        assertEquals(topLevelTransactionMapper, actualMapper);
    }

    @Test
    void shouldReturnAnnotationTransactionMapperWhenAnnotationKindPassed() {
        // given

        // when
        AbstractTransactionMapper actualMapper = factory.getTransactionMapper(ANNOTATION);

        // then
        assertInstanceOf(AnnotationTransactionMapper.class, actualMapper);
        assertEquals(annotationTransactionMapper, actualMapper);
    }

    @Test
    void shouldReturnAssociatedFilingTransactionMapperWhenAnnotationKindPassed() {
        // given

        // when
        AbstractTransactionMapper actualMapper = factory.getTransactionMapper(ASSOCIATED_FILING);

        // then
        assertInstanceOf(AssociatedFilingTransactionMapper.class, actualMapper);
        assertEquals(associatedFilingTransactionMapper, actualMapper);
    }

    @Test
    void shouldReturnResolutionFilingTransactionMapperWhenResolutionKindPassed() {
        // given

        // when
        AbstractTransactionMapper actualMapper = factory.getTransactionMapper(RESOLUTION);

        // then
        assertInstanceOf(ResolutionTransactionMapper.class, actualMapper);
        assertEquals(resolutionTransactionMapper, actualMapper);
    }
}
