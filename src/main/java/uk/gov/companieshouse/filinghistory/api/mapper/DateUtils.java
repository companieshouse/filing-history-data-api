package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.Instant;
import java.util.Optional;

public final class DateUtils {


    private DateUtils() {
    }

    static Instant stringToInstant(final String dateString) {
        return Optional.ofNullable(dateString)
                .map(Instant::parse)
                .orElse(null);
    }
}