package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.Resolution;

public class ResolutionCleanserTest {

    private final ResolutionCleanser resolutionCleanser = new ResolutionCleanser();

    @Test
    void shouldRemoveDeltaAtsFromResolutions() {
        // given
        List<Resolution> resolutions = List.of(
                new Resolution()
                        .deltaAt("1234"),
                new Resolution()
                        .deltaAt("5678"));

        List<Resolution> expected = List.of(
                new Resolution(),
                new Resolution());

        // when
        List<Resolution> actual = resolutionCleanser.removeDeltaAt(resolutions);

        // then
        assertEquals(expected, actual);
    }
}
