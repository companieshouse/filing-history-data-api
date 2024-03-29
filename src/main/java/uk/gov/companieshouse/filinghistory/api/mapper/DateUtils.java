package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

public final class DateUtils {

    private DateUtils() {
    }

    public static Instant stringToInstant(final String dateString) {
        return Optional.ofNullable(dateString)
                .map(Instant::parse)
                .orElse(null);
    }

    public static String instantToString(final Instant inputDate) {
        return Optional.ofNullable(inputDate)
                .map(date -> LocalDate.ofInstant(date, ZoneOffset.UTC).toString())
                .orElse(null);
    }
}