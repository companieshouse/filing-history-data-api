package uk.gov.companieshouse.filinghistory.api.interceptor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationInterceptorITest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn200FromGetHealthEndpoint() throws Exception {
        this.mockMvc.perform(get("/filing-history-data-api/healthcheck"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"status\":\"UP\"}"));
    }

    @Test
    void shouldReturn401FromEndpointWithoutAuth() throws Exception {
        this.mockMvc.perform(get("/test"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}