package searchengine.web.services.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private boolean result;
    private long count;
    private List<SearchResult> data;
}
