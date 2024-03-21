package uk.gov.companieshouse.filinghistory.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.filinghistory.api.model.statusrules.StatusRuleProperties;
import uk.gov.companieshouse.filinghistory.api.service.CompanyNumberStatusProcessor;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryStatusService;
import uk.gov.companieshouse.filinghistory.api.service.StatusService;

@Configuration
public class StatusConfig {

    @Bean
    public StatusService statusRules(@Value("${status.rules}") String rulesFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream stream = getClass().getResourceAsStream("/%s".formatted(rulesFile));

        return new FilingHistoryStatusService(
                mapper.readValue(stream, StatusRuleProperties.class),
                new CompanyNumberStatusProcessor());
    }
}
