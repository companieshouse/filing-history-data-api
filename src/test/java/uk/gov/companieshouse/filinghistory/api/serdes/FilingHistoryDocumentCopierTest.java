package uk.gov.companieshouse.filinghistory.api.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.exception.InternalServerErrorException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDocumentCopierTest {

    @InjectMocks
    private FilingHistoryDocumentCopier copier;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FilingHistoryDocument inputDocument;
    @Mock
    private FilingHistoryDocument copyInputDocument;

    @Test
    void shouldReturnCopyOfInputDocument() throws Exception {
        // given
        when(objectMapper.writeValueAsString(any())).thenReturn("object_as_string");
        when(objectMapper.readValue(anyString(), eq(FilingHistoryDocument.class))).thenReturn(copyInputDocument);

        // when
        FilingHistoryDocument copyDocument = copier.deepCopy(inputDocument);

        // then
        assertNotNull(copyDocument);
        assertEquals(copyDocument, copyInputDocument);
        assertNotEquals(copyDocument, inputDocument);
    }

    @Test
    void shouldThrowInternalServerErrorWhenJsonProcessingExceptionThrownOnWrite() throws Exception {
        // given
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        // when
        Executable executable = () -> copier.deepCopy(inputDocument);

        // then
        assertThrows(InternalServerErrorException.class, executable);
    }

    @Test
    void shouldThrowInternalServerErrorWhenJsonProcessingExceptionThrownOnRead() throws Exception {
        // given
        when(objectMapper.writeValueAsString(any())).thenReturn("object_as_string");
        when(objectMapper.readValue(anyString(), eq(FilingHistoryDocument.class))).thenThrow(JsonProcessingException.class);

        // when
        Executable executable = () -> copier.deepCopy(inputDocument);

        // then
        assertThrows(InternalServerErrorException.class, executable);
    }
}
