package searchengine.mapper.delegates;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.core.dto.LemmaDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.mapper.LemmaMapper;
import searchengine.mapper.SearchIndexMapper;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.SearchIndex;
import searchengine.model.services.LemmaService;

@Component
@RequiredArgsConstructor
public class SearchIndexMapperDelegate implements SearchIndexMapper {

    private final LemmaService lemmaService;
    private final LemmaMapper lemmaMapper;

    @Override
    public SearchIndex dtoToEntity(SearchIndexDto indexDto) {
        SearchIndex index = new SearchIndex();
        index.setPage(indexDto.getPage());
        index.setLemma(lemmaSaveOrUpdateFromDto(indexDto.getLemma()));
        index.setRank(indexDto.getRank());
        return index;
    }

    @Transactional
    public Lemma lemmaSaveOrUpdateFromDto(LemmaDto lemmaDto) {
        Lemma lemma;
        try{
            lemma = lemmaService.findByLemmaAndSite(lemmaDto.getLemma(), lemmaDto.getSite());
            return lemmaService.update(lemmaMapper.dtoToEntity(lemma.getId(),lemmaDto, lemma.getFrequency()));
        } catch (NoFoundEntityException e){
            return lemmaService.create(lemmaMapper.dtoToEntity(lemmaDto));
        }
    }
}
