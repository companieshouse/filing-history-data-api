package uk.gov.companieshouse.filinghistory.api.config;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OpenTelemetryAppenderInitializerTest {
    @Test
    void shouldInstallOpenTelemetryAppenderOnInitialization() {
        OpenTelemetryAppenderInitializer initializer =
                new OpenTelemetryAppenderInitializer(OpenTelemetry.noop());

        assertDoesNotThrow(initializer::afterPropertiesSet);
    }
}
