package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@ExtendWith(MockitoExtension.class)
class ResourceChangedRequestMapperTest {

    private static final String EXPECTED_CONTEXT_ID = "35234234";
    private static final Object deletedData = new Object();

    @Mock
    private Supplier<Instant> instantSupplier;

    @InjectMocks
    private ResourceChangedRequestMapper mapper;

    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);


    @ParameterizedTest
    @MethodSource("resourceChangedScenarios")
    void testMapper(ResourceChangedTestArgument argument) {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);

        // when
        ChangedResource actual = mapper.mapChangedResource(argument.getRequest());

        // then
        assertEquals(argument.getChangedResource(), actual);
    }

    private static Stream<Arguments> resourceChangedScenarios() {
        return Stream.of(
                Arguments.of(
                        Named.of("Test resource-changed scenario with event type of changed",
                        ResourceChangedTestArgument.ResourceChangedTestArgumentBuilder()
                                .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "12345678", "ABCDE54321", null, false))
                                .withContextId(EXPECTED_CONTEXT_ID)
                                .withResourceUri("/company/12345678/filing-history/ABCDE54321")
                                .withResourceKind("filing-history")
                                .withEventType("changed")
                                .withEventPublishedAt(UPDATED_AT.toString())
                                .build()
                        )
                ),
                Arguments.of(
                    Named.of("Test resource-changed scenario with event type of deleted",
                    ResourceChangedTestArgument.ResourceChangedTestArgumentBuilder()
                            .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "12345678", "ABCDE54321",
                                    deletedData, true))
                            .withContextId(EXPECTED_CONTEXT_ID)
                            .withResourceUri("/company/12345678/filing-history/ABCDE54321")
                            .withResourceKind("filing-history")
                            .withEventType("deleted")
                            .withDeletedData(deletedData)
                            .withEventPublishedAt(UPDATED_AT.toString())
                            .build()
                    )
                )
        );
    }

    private static class ResourceChangedTestArgument {
        private final ResourceChangedRequest request;
        private final ChangedResource changedResource;

        public ResourceChangedTestArgument(ResourceChangedRequest request, ChangedResource changedResource) {
            this.request = request;
            this.changedResource = changedResource;
        }

        public static ResourceChangedTestArgumentBuilder ResourceChangedTestArgumentBuilder() {
            return new ResourceChangedTestArgumentBuilder();
        }

        public ResourceChangedRequest getRequest() {
            return request;
        }

        public ChangedResource getChangedResource() {
            return changedResource;
        }

        private static class ResourceChangedTestArgumentBuilder {
            private ResourceChangedRequest request;
            private String resourceUri;
            private String resourceKind;
            private String contextId;
            private String eventType;
            private String eventPublishedAt;
            private Object deletedData;

            public ResourceChangedTestArgumentBuilder withRequest(ResourceChangedRequest request) {
                this.request = request;
                return this;
            }

            public ResourceChangedTestArgumentBuilder withResourceUri(String resourceUri) {
                this.resourceUri = resourceUri;
                return this;
            }

            public ResourceChangedTestArgumentBuilder withResourceKind(String resourceKind) {
                this.resourceKind = resourceKind;
                return this;
            }

            public ResourceChangedTestArgumentBuilder withContextId(String contextId) {
                this.contextId = contextId;
                return this;
            }

            public ResourceChangedTestArgumentBuilder withEventType(String eventType) {
                this.eventType = eventType;
                return this;
            }

            public ResourceChangedTestArgumentBuilder withEventPublishedAt(String eventPublishedAt) {
                this.eventPublishedAt = eventPublishedAt;
                return this;
            }

            public ResourceChangedTestArgumentBuilder withDeletedData(Object deletedData) {
                this.deletedData = deletedData;
                return this;
            }

            public ResourceChangedTestArgument build() {
                ChangedResource changedResource = new ChangedResource();
                changedResource.setResourceUri(this.resourceUri);
                changedResource.setResourceKind(this.resourceKind);
                changedResource.setContextId(this.contextId);
                ChangedResourceEvent event = new ChangedResourceEvent();
                event.setType(this.eventType);
                event.setPublishedAt(this.eventPublishedAt);
                changedResource.setEvent(event);
                changedResource.setDeletedData(deletedData);
                return new ResourceChangedTestArgument(this.request, changedResource);
            }
        }

        @Override
        public String toString() {
            return this.request.toString();
        }
    }
}
