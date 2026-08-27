package uk.gov.companieshouse.filinghistory.api.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.api.model.statusrules.StatusRuleProperties;

class StatusConfigTest {

    private final StatusConfig statusConfig = new StatusConfig();

    @Test
    void shouldLoadStatusRulesFromYaml() {
        StatusRuleProperties statusRuleProperties = statusConfig.statusRules("status_field_rules.yml");

        assertNotNull(statusRuleProperties);
        assertNotNull(statusRuleProperties.filingHistory());
        assertFalse(statusRuleProperties.filingHistory().isEmpty());
    }

    @Test
    void shouldLoadAllExpectedPrefixesFromYaml() {
        StatusRuleProperties statusRuleProperties = statusConfig.statusRules("status_field_rules.yml");

        assertTrue(statusRuleProperties.filingHistory().containsKey("AC"));
        assertTrue(statusRuleProperties.filingHistory().containsKey("NORMAL"));
        assertTrue(statusRuleProperties.filingHistory().containsKey("UNKNOWN_PREFIX"));
    }

    @Test
    void shouldLoadPrefixPropertiesWithStatus() {
        StatusRuleProperties statusRuleProperties = statusConfig.statusRules("status_field_rules.yml");

        var normalPrefix = statusRuleProperties.filingHistory().get("NORMAL");
        assertNotNull(normalPrefix);
        assertNotNull(normalPrefix.type());
        assertNotNull(normalPrefix.status());
    }

    @Test
    void shouldLoadPrefixPropertiesWithFromRules() {
        StatusRuleProperties statusRuleProperties = statusConfig.statusRules("status_field_rules.yml");

        var lpPrefix = statusRuleProperties.filingHistory().get("LP");
        assertNotNull(lpPrefix);
        assertNotNull(lpPrefix.from());
        assertFalse(lpPrefix.from().isEmpty());
    }
}
