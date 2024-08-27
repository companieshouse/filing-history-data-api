package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.serdes.FilingHistoryDocumentCopier;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DeleteMapperDelegator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);
    private static final String COMPOSITE_RES_TYPE = "RESOLUTIONS";
    private final FilingHistoryDocumentCopier documentCopier;
    private final CompositeResolutionDeleteMapper compositeResolutionDeleteMapper;
    private final ChildDeleteMapper childDeleteMapper;

    public DeleteMapperDelegator(FilingHistoryDocumentCopier documentCopier,
            CompositeResolutionDeleteMapper compositeResolutionDeleteMapper, ChildDeleteMapper childDeleteMapper) {
        this.documentCopier = documentCopier;
        this.compositeResolutionDeleteMapper = compositeResolutionDeleteMapper;
        this.childDeleteMapper = childDeleteMapper;
    }

    @DeleteChildTransactions
    public Optional<FilingHistoryDocument> delegateDelete(String entityId, FilingHistoryDeleteAggregate aggregate) {
        FilingHistoryDocument document = documentCopier.deepCopy(aggregate.getDocument());
        FilingHistoryData data = document.getData();

        final int resIndex = aggregate.getResolutionIndex();
        if (resIndex >= 0) {
            if (COMPOSITE_RES_TYPE.equals(data.getType())) {
                LOGGER.debug("Matched composite resolution", DataMapHolder.getLogMap());
                return compositeResolutionDeleteMapper.removeTransaction(resIndex, document);
            } else {
                LOGGER.debug("Matched resolution", DataMapHolder.getLogMap());
                return childDeleteMapper.removeTransaction(entityId, resIndex, document, data::getResolutions,
                        data::resolutions);
            }
        }

        final int annotationIndex = aggregate.getAnnotationIndex();
        if (annotationIndex >= 0) {
            LOGGER.debug("Matched annotation", DataMapHolder.getLogMap());
            return childDeleteMapper.removeTransaction(entityId, annotationIndex, document, data::getAnnotations,
                    data::annotations);
        }

        final int associatedFilingIndex = aggregate.getAssociatedFilingIndex();
        if (associatedFilingIndex >= 0) {
            LOGGER.debug("Matched associated filing", DataMapHolder.getLogMap());
            return childDeleteMapper.removeTransaction(entityId, associatedFilingIndex, document,
                    data::getAssociatedFilings,
                    data::associatedFilings);
        }

        if (entityId.equals(document.getEntityId())) {
            return Optional.empty();
        } else {
            LOGGER.error("No match on entity id", DataMapHolder.getLogMap());
            throw new BadRequestException("No match on entity id");
        }
    }
}
