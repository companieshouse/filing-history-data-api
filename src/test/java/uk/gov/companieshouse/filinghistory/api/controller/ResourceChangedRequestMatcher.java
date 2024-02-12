package uk.gov.companieshouse.filinghistory.api.controller;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import uk.gov.companieshouse.api.chskafka.ChangedResource;

public class ResourceChangedRequestMatcher implements ValueMatcher<Request> {

    private static final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(Include.NON_EMPTY)
            .registerModule(new JavaTimeModule());
    private final String expectedUrl;
    private final ChangedResource expectedBody;

    public ResourceChangedRequestMatcher(String expectedUrl, ChangedResource expectedBody) {
        this.expectedUrl = expectedUrl;
        this.expectedBody = expectedBody;
    }

    @Override
    public MatchResult match(Request value) {
        return MatchResult.aggregate(
                matchUrl(value.getUrl()),
                matchMethod(value.getMethod()),
                matchBody(value.getBodyAsString()));
    }

    private MatchResult matchUrl(String actualUrl) {
        return MatchResult.of(expectedUrl.equals(actualUrl));
    }

    private MatchResult matchMethod(RequestMethod actualMethod) {
        return MatchResult.of(RequestMethod.POST.equals(actualMethod));
    }

    private MatchResult matchBody(String actualBody) {
        try {
            ChangedResource actual = mapper.readValue(actualBody, ChangedResource.class);
            return MatchResult.of(expectedBody.equals(actual));
        } catch (JsonProcessingException e) {
            return MatchResult.of(false);
        }
    }
}

