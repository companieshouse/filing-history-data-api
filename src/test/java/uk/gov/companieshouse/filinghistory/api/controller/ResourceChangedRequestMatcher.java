package uk.gov.companieshouse.filinghistory.api.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.chskafka.ChangedResource;

public class ResourceChangedRequestMatcher implements ValueMatcher<Request> {

    private static final JsonMapper mapper = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl
                    .withValueInclusion(JsonInclude.Include.NON_EMPTY)
                    .withContentInclusion(JsonInclude.Include.NON_EMPTY))
            .build();
    private final String expectedUrl;
    private final String expectedBody;

    public ResourceChangedRequestMatcher(String expectedUrl, String expectedBody) {
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
            actual.deletedData(
                    mapper.readValue(mapper.writeValueAsString(actual.getDeletedData()), Object.class));

            ChangedResource expected = mapper.readValue(expectedBody, ChangedResource.class);
            expected.deletedData(
                    mapper.readValue(mapper.writeValueAsString(expected.getDeletedData()), Object.class));

            MatchResult result = MatchResult.of(expected.equals(actual));
            if (!result.isExactMatch()) {
                System.out.printf("%nExpected: [%s]%n", expected);
                System.out.printf("%nActual: [%s]", actual);
            }
            return result;
        } catch (JacksonException e) {
            return MatchResult.of(false);
        }
    }
}

