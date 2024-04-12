package uk.gov.companieshouse.filinghistory.api.mapper.get;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.Resolution;

@Component
public class ResolutionCleanser {

    List<Resolution> removeDeltaAt(List<Resolution> resolutions) {
        resolutions.forEach(resolution -> resolution.deltaAt(null));
        return resolutions;
    }
}
