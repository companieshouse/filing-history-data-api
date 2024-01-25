package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(UTC);

    private DateUtils() {
    }

    static Instant stringToInstant(final String dateString) {
        return Optional.ofNullable(dateString)
                .map(date -> Instant.from(FORMATTER.parse(date)))
                .orElse(null);
    }
}