# filing-history-data-api
The filing-history-data-api is a Java service that transforms filing history deltas to a standardised structure then stores these to the company_filing_history collection in MongoDB. On completion the filing-history-data-api enqueues a resource changed message that triggers further downstream processing.
