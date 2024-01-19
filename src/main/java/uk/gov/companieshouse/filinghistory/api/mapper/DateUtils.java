package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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