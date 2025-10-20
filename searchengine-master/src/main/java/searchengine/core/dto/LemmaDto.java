package searchengine.core.dto;

import lombok.Data;
import searchengine.model.entity.SiteEntity;

@Data
public class LemmaDto {

    private SiteEntity site;
    private String lemma;
}
