package uk.gov.companieshouse.filinghistory.api.mapper.get;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class DateUtils {

    private DateUtils() {
    }

    static String convertInstantToLocalDateString(final Instant inputDate) {
        return inputDate == null ? null : LocalDate.ofInstant(inputDate, ZoneOffset.UTC).toString();
    }
}
