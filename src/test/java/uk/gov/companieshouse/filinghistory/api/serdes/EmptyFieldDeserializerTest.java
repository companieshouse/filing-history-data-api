package uk.gov.companieshouse.filinghistory.api.serdes;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.api.config.AppConfig;

class EmptyFieldDeserializerTest {

    private static final String JSON = """
            {
                  "stuff": "stuff",
                  "optional_stuff": "",
                  "unknown_stuff": ""
            },
            """;

    private record Stuff(String stuff, String optional_stuff) {

    }

    private final ObjectMapper objectMapper = new AppConfig().objectMapper();

    @Test
    void successfullyDeserialize() throws Exception {
        // when
        Stuff result = objectMapper.readValue(JSON, Stuff.class);

        // then
        assertNull(result.optional_stuff());
        assertNotNull(result.stuff());
    }
}
