package uk.gov.companieshouse.filinghistory.api.util;

public class SonarCheck {

    public String sonarCoverageCheck() {
        double rand = Math.random();
        if (rand % 2 == 0) {
            return "even";
        } else {
            return "odd";
        }
    }
}
