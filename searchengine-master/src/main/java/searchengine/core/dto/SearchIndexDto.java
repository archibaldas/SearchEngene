package searchengine.core.dto;

import lombok.Data;
import searchengine.model.entity.Page;

@Data
public class SearchIndexDto {

    private Page page;
    private LemmaDto lemma;
    private Float rank;
}
