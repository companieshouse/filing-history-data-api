package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.Resolution;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResolutionCleanser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    List<Resolution> removeDeltaAt(List<Resolution> resolutions) {
        resolutions.forEach(resolution -> resolution.deltaAt(null));
        return resolutions;
    }
}
