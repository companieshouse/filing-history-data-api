package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
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

    public Optional<FilingHistoryDocument> delegateDelete(String entityId, FilingHistoryDocument existingDocument,
            String requestDeltaAt) {
        FilingHistoryDocument document = documentCopier.deepCopy(existingDocument);
        FilingHistoryData data = document.getData();

        final int resIndex = doIndexOf(entityId, data::getResolutions);
        if (resIndex >= 0) {
            if (COMPOSITE_RES_TYPE.equals(data.getType())) {
                LOGGER.debug("Matched composite resolution", DataMapHolder.getLogMap());
                return compositeResolutionDeleteMapper.removeTransaction(resIndex, requestDeltaAt, document);
            } else {
                LOGGER.debug("Matched resolution", DataMapHolder.getLogMap());
                return childDeleteMapper.removeTransaction(entityId, requestDeltaAt, resIndex, document,
                        data::getResolutions, data::resolutions);
            }
        }

        final int annotationIndex = doIndexOf(entityId, data::getAnnotations);
        if (annotationIndex >= 0) {
            LOGGER.debug("Matched annotation", DataMapHolder.getLogMap());
            return childDeleteMapper.removeTransaction(entityId, requestDeltaAt, annotationIndex, document,
                    data::getAnnotations, data::annotations);
        }

        final int associatedFilingIndex = doIndexOf(entityId, data::getAssociatedFilings);
        if (associatedFilingIndex >= 0) {
            LOGGER.debug("Matched associated filing", DataMapHolder.getLogMap());
            return childDeleteMapper.removeTransaction(entityId, requestDeltaAt, associatedFilingIndex, document,
                    data::getAssociatedFilings, data::associatedFilings);
        }

        if (entityId.equals(document.getEntityId())) {
            return Optional.empty();
        } else {
            LOGGER.info("No match on entity id, child already deleted", DataMapHolder.getLogMap());
            return Optional.of(document);
        }
    }

    private static <T extends FilingHistoryChild> Integer doIndexOf(String entityId, Supplier<List<T>> childList) {
        return Optional.ofNullable(childList.get())
                .map(children -> children.stream()
                        .map(FilingHistoryChild::getEntityId)
                        .toList()
                        .indexOf(entityId))
                .orElse(-1);
    }
}
