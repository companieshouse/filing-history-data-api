package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;

public final class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);

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

    public static String publishedAtString(final Instant source) {
        return source.atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"));
    }

    public static boolean isDeltaStale(final String requestDeltaAt, final String existingDeltaAt) {
        return StringUtils.isNotBlank(existingDeltaAt) && !OffsetDateTime.parse(requestDeltaAt, FORMATTER)
                .isAfter(OffsetDateTime.parse(existingDeltaAt, FORMATTER));
    }
}