package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import org.springframework.stereotype.Component;

@Component
public class DeleteMapperFactory {

    private final ResolutionDeleteMapper resolutionDeleteMapper;


    public DeleteMapperFactory(ResolutionDeleteMapper resolutionDeleteMapper) {
        this.resolutionDeleteMapper = resolutionDeleteMapper;
    }

    public DeleteMapper getDeleteMapper(String type){
        return null;
    }
}
