package uk.gov.companieshouse.filinghistory.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilingHistoryApplication {

    public static final String NAMESPACE = "filing-history-data-api";

    public static void main(String[] args) {
        SpringApplication.run(FilingHistoryApplication.class, args);
    }
}
