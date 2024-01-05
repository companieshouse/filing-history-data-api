package uk.gov.companieshouse.filinghistory.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@AutoConfigureMockMvc
@SpringBootTest(classes = FilingHistoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilingHistoryApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Test
    void shouldLoadApplicationContext() {
        assertNotNull(context);
    }

    @Test
    void shouldReturn200FromGetHealthEndpoint() throws Exception {
        this.mockMvc.perform(get("/filing-history-data-api/healthcheck"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"status\":\"UP\"}"));
    }
}
