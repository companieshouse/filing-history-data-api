package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class DateUtils {

    private DateUtils() {
    }

    static Instant localDateToInstant(final LocalDate localDate) {
        return localDate != null ? Instant.from(localDate.atStartOfDay(ZoneOffset.UTC)) : null;
    }
}