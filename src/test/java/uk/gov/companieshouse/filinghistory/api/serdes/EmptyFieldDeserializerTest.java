package uk.gov.companieshouse.filinghistory.api.serdes;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

class EmptyFieldDeserializerTest {

    private static final String JSON = """
            {
                  "stuff": "stuff",
                  "optional_stuff": "",
                  "unknown_stuff": ""
            }
            """;

    private record Stuff(String stuff, String optional_stuff) {

    }

    private final JsonMapper objectMapper = JsonMapper.builder()
            .addModule(new SimpleModule().addDeserializer(String.class, new EmptyFieldDeserializer()))
            .build();

    @Test
    void successfullyDeserialize() throws Exception {
        // when
        Stuff result = objectMapper.readValue(JSON, Stuff.class);

        // then
        assertNull(result.optional_stuff());
        assertNotNull(result.stuff());
    }
}
