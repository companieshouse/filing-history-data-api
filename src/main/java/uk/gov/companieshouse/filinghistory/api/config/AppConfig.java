package uk.gov.companieshouse.filinghistory.api.config;

import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.module.SimpleModule;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.ChildListMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.ChildMapper;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;
import uk.gov.companieshouse.filinghistory.api.serdes.EmptyFieldDeserializer;

@Configuration
public class AppConfig {

    @Bean
    public Supplier<Instant> instantSupplier() {
        return Instant::now;
    }

    @Bean
    public Supplier<InternalApiClient> internalApiClientSupplier(
            @Value("${api.api-key}") String apiKey,
            @Value("${api.api-url}") String apiUrl) {
        return () -> {
            InternalApiClient internalApiClient = new InternalApiClient(new ApiKeyHttpClient(
                    apiKey));
            internalApiClient.setBasePath(apiUrl);
            return internalApiClient;
        };
    }

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .addModule(new SimpleModule().addDeserializer(String.class, new EmptyFieldDeserializer()));
    }

    @Bean
    public ChildListMapper<FilingHistoryAnnotation> annotationChildListMapper(
            ChildMapper<FilingHistoryAnnotation> childMapper) {
        return new ChildListMapper<>(childMapper);
    }

    @Bean
    public ChildListMapper<FilingHistoryAssociatedFiling> associatedFilingChildListMapper(
            ChildMapper<FilingHistoryAssociatedFiling> childMapper) {
        return new ChildListMapper<>(childMapper);
    }

    @Bean
    public ChildListMapper<FilingHistoryResolution> resolutionChildListMapper(
            ChildMapper<FilingHistoryResolution> childMapper) {
        return new ChildListMapper<>(childMapper);
    }
}
