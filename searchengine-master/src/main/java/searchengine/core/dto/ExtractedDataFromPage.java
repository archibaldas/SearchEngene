package searchengine.core.dto;

import lombok.Data;
import searchengine.model.entity.Page;

import java.util.List;
import java.util.Map;

@Data
public class ExtractedDataFromPage {
    private Page page;
    private List<String> childLinks;
    private Map<String, Integer> lemmaMap;
}
