package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;

@Component
public class RemoveChildMapper<T extends FilingHistoryChild>  {

    public List<T> removeChild(final String entityId, List<T> existingChildList){
        return null;
    }
}
