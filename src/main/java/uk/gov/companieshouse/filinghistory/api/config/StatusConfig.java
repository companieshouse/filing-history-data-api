package uk.gov.companieshouse.filinghistory.api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryStatusService;
import uk.gov.companieshouse.filinghistory.api.service.StatusService;
import uk.gov.companieshouse.filinghistory.api.statusrules.functions.StatusFactory;
import uk.gov.companieshouse.filinghistory.api.statusrules.parsers.RuleProperties;

@Configuration
public class StatusConfig {

    @Bean
    public StatusService statusRules(@Value("${status.rules}") String rulesFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream stream = getClass().getResourceAsStream("/%s".formatted(rulesFile));

        RuleProperties ruleProperties = mapper.readValue(stream, new TypeReference<>(){});

        return new FilingHistoryStatusService(ruleProperties);
    }
}
