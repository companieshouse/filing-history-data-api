package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public final class DateUtils {

    private DateUtils() {
    }

    static Instant localDateToInstant(final LocalDate localDate) {
        return Optional.ofNullable(localDate)
                .map(date -> Instant.from(date.atStartOfDay(UTC)))
                .orElse(null);
    }
}