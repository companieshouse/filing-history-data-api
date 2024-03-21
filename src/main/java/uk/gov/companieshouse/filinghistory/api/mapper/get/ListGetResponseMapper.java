package uk.gov.companieshouse.filinghistory.api.mapper.get;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList.FilingHistoryStatusEnum;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListAggregate;

@Component
public class ListGetResponseMapper {

    private final ItemGetResponseMapper itemGetResponseMapper;

    public ListGetResponseMapper(ItemGetResponseMapper itemGetResponseMapper) {
        this.itemGetResponseMapper = itemGetResponseMapper;
    }

    public FilingHistoryList mapBaseFilingHistoryList(int startIndex, int itemsPerPage, String status) {
        return new FilingHistoryList()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .totalCount(0)
                .filingHistoryStatus(FilingHistoryStatusEnum.fromValue(status));
    }

    public FilingHistoryList mapFilingHistoryList(int startIndex, int itemsPerPage, String status,
            FilingHistoryListAggregate listAggregate) {
        return new FilingHistoryList()
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .filingHistoryStatus(FilingHistoryStatusEnum.fromValue(status))
                .totalCount(listAggregate.getTotalCount())
                .items(listAggregate.getDocumentList().stream()
                        .map(itemGetResponseMapper::mapFilingHistoryItem)
                        .toList());
    }
}
