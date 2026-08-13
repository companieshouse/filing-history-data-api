package uk.gov.companieshouse.filinghistory.api.serdes;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class EmptyFieldDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext context)
            throws JacksonException {
        JsonNode node = jsonParser.readValueAsTree();
        String str = node.asText();
        if (str.isEmpty()) {
            return null;
        }
        return str;
    }
}
