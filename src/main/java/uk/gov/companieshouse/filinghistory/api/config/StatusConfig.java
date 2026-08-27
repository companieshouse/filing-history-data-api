package uk.gov.companieshouse.filinghistory.api.config;

import tools.jackson.dataformat.yaml.YAMLMapper;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.filinghistory.api.model.statusrules.StatusRuleProperties;

@Configuration
public class StatusConfig {

    @Bean
    public StatusRuleProperties statusRules(@Value("${status.rules}") String rulesFile) {
        YAMLMapper mapper = new YAMLMapper();
        InputStream stream = getClass().getResourceAsStream("/%s".formatted(rulesFile));

        return mapper.readValue(stream, StatusRuleProperties.class);
    }
}
