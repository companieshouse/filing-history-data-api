package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.InvalidMapperException;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.TopLevelTransactionMapper;

@ExtendWith(MockitoExtension.class)
class AbstractTransactionMapperFactoryTest {

    @InjectMocks
    private AbstractTransactionMapperFactory factory;

    @Mock
    private TopLevelTransactionMapper topLevelTransactionMapper;

    @Test
    void shouldReturnTopLevelTransactionMapperWhenTopLevelKindPassed() {
        // given

        // when
        AbstractTransactionMapper actualMapper = factory.getTransactionMapper(TOP_LEVEL);

        // then
        assertInstanceOf(TopLevelTransactionMapper.class, actualMapper);
        assertEquals(topLevelTransactionMapper, actualMapper);
    }

    @ParameterizedTest
    @CsvSource({"annotation", "resolution", "associated-filing"})
    void shouldReturnInvalidMapperExceptionWhenKindDoesNotReturnAMapper(String kind) {
        // given

        // when
        Executable executable = () -> factory.getTransactionMapper(TransactionKindEnum.fromValue(kind));

        // then
        InvalidMapperException exception = assertThrows(InvalidMapperException.class, executable);
        assertEquals("Invalid transaction kind: %s".formatted(kind), exception.getMessage());
    }
}