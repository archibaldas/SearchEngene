package searchengine.mapper.delegates;

import org.springframework.stereotype.Component;
import searchengine.core.dto.LemmaDto;
import searchengine.mapper.LemmaMapper;
import searchengine.model.entity.Lemma;

@Component
public class LemmaMapperDelegate implements LemmaMapper {
    @Override
    public Lemma dtoToEntity(LemmaDto lemmaDto) {
        Lemma lemma = new Lemma();
        lemma.setLemma(lemmaDto.getLemma());
        lemma.setSite(lemmaDto.getSite());
        lemma.setFrequency(1);
        return lemma;
    }

    @Override
    public Lemma dtoToEntity(LemmaDto lemmaDto, Integer frequency) {
        Lemma lemma = dtoToEntity(lemmaDto);
        lemma.setFrequency(frequency + lemma.getFrequency());
        return lemma;
    }

    @Override
    public Lemma dtoToEntity(Long id, LemmaDto lemmaDto, Integer frequency) {
        Lemma lemma = dtoToEntity(lemmaDto, frequency);
        lemma.setId(id);
        return lemma;
    }
}
