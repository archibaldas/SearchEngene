package searchengine.mapper;

import searchengine.core.dto.LemmaDto;
import searchengine.model.entity.Lemma;

public interface LemmaMapper {
    Lemma dtoToEntity (LemmaDto lemmaDto);
    Lemma dtoToEntity (LemmaDto lemmaDto, Integer frequency);
    Lemma dtoToEntity (Long id, LemmaDto lemmaDto, Integer frequency);
}
