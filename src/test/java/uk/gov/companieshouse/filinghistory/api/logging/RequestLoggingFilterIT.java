package uk.gov.companieshouse.filinghistory.api.logging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryUpsertProcessor;

@AutoConfigureMockMvc
@SpringBootTest
class RequestLoggingFilterIT {

    private static final String PUT_REQUEST_URI = "/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}/internal";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final String ENTITY_ID = "1234567890";
    private static final String DOCUMENT_ID = "000X4BI89B65846";
    private static final String BARCODE = "X4BI89B6";
    private static final String NEWEST_REQUEST_DELTA_AT = "20140916230459600643";
    private static final String UPDATED_BY = "5419d856b6a59f32b7684d0e";
    private static final String TM01_TYPE = "TM01";
    private static final String DATE = "2014-09-15T23:21:18.000Z";
    private static final String ORIGINAL_DESCRIPTION = "Appointment Terminated, Director john tester";
    private static final String OFFICER_NAME = "John Tester";
    private static final String RESIGNATION_DATE = "29/08/2014";
    private static final String DESCRIPTION = "termination-director-company-with-name-termination-date";
    private static final String ACTION_AND_TERMINATION_DATE = "2014-08-29T00:00:00.000Z";
    private static final String SUBCATEGORY = "termination";
    private static final String CONTEXT_ID = "ABCD1234";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilingHistoryUpsertProcessor upsertProcessor;

    @Test
    void shouldThrowServletExceptionWhenRuntimeExceptionCaught() {
        // given
        InternalFilingHistoryApi request = buildPutRequestBody();

        doThrow(RuntimeException.class)
                .when(upsertProcessor).processFilingHistory(any(), any(), any());

        // when
        Executable executable = () -> mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        assertThrows(ServletException.class, executable);
    }

    private static InternalFilingHistoryApi buildPutRequestBody() {
        return new InternalFilingHistoryApi()
                .internalData(buildInternalData())
                .externalData(buildExternalData());
    }

    private static InternalData buildInternalData() {
        return new InternalData()
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .originalValues(new InternalDataOriginalValues()
                        .officerName(OFFICER_NAME)
                        .resignationDate(RESIGNATION_DATE))
                .parentEntityId("parent_entity_id")
                .updatedBy(UPDATED_BY)
                .transactionKind(InternalData.TransactionKindEnum.TOP_LEVEL);
    }

    private static ExternalData buildExternalData() {
        return new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .type(TM01_TYPE)
                .date(DATE)
                .category(ExternalData.CategoryEnum.OFFICERS)
                .annotations(null)
                .subcategory(SUBCATEGORY)
                .description(DESCRIPTION)
                .descriptionValues(new DescriptionValues()
                        .officerName(OFFICER_NAME)
                        .terminationDate(ACTION_AND_TERMINATION_DATE))
                .pages(1) // should not be mapped, persisted by document store sub delta
                .actionDate(ACTION_AND_TERMINATION_DATE)
                .links(new Links()
                        .self(SELF_LINK));
    }
}
