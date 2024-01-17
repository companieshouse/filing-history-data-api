package uk.gov.companieshouse.filinghistory.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class FilingHistoryControllerTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private FilingHistoryController controller;

    @Mock
    private FilingHistoryService service;

    @Mock
    private InternalFilingHistoryApi requestBody;

    @Test
    void shouldReturn200OKWhenPutRequest() {
        // given
        final ResponseEntity<Void> expectedResponse = ResponseEntity.status(HttpStatus.OK).build();

        // when
        final ResponseEntity<Void> actualResponse = controller.upsertFilingHistoryTransaction(COMPANY_NUMBER, TRANSACTION_ID, requestBody);

        // then
        assertEquals(expectedResponse, actualResponse);
    }
}
