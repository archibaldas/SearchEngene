package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.core.utils.BeanUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.SiteEntity;
import searchengine.model.projection.GlobalLemmaFrequencyProjection;
import searchengine.model.projection.LemmaFrequencyProjection;
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.services.LemmaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {

    private final JdbcTemplate jdbcTemplate;
    private final LemmaRepository lemmaRepository;

    @Override
    public Lemma findById(Long id) {
        return lemmaRepository.findById(id)
                .orElseThrow(() -> new NoFoundEntityException("Лемма с", id, " не найдена."));
    }

    @Override
    @Transactional
    public void updateLemmasForSite(SiteEntity site, Map<String, Integer> lemmasOnPage) {
        if(lemmasOnPage.isEmpty()) return;

        String sql = """
                INSERT INTO lemma (site_id, lemma, frequency) 
                VALUES (?, ?, 1) ON CONFLICT (site_id, lemma) 
                DO UPDATE SET frequency = lemma.frequency + 1
                """;

        List<Object[]> batchArgs = new ArrayList<>();
        for(String lemma : lemmasOnPage.keySet()){
            batchArgs.add(new Object[]{site.getId(), lemma});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public int count() {
        return Math.toIntExact(lemmaRepository.count());
    }

    @Override
    @Transactional
    public void update(Lemma entity) {
        Lemma updatedLemma = findById(entity.getId());
        BeanUtils.copyNotNullProperties(entity, updatedLemma);
        lemmaRepository.save(updatedLemma);
    }

    @Override
    @Transactional
    public void delete(Lemma entity) {
        lemmaRepository.delete(entity);
    }

    @Override
    public List<Lemma> findBySiteAndLemmaIn(SiteEntity site, Set<String> lemmaSet) {
        return lemmaRepository.findBySiteAndLemmaIn(site, lemmaSet);
    }

    @Override
    public Map<String, Integer> getFrequenciesBySiteAndLemmas(SiteEntity site, List<String> lemmas) {
        List<LemmaFrequencyProjection> results = lemmaRepository.findFrequenciesBySiteAndLemmas(site, lemmas);
        return results.stream()
                .collect(Collectors.toMap(
                        LemmaFrequencyProjection::getLemma,
                        LemmaFrequencyProjection::getFrequency
                ));
    }

    @Override
    public Map<String, Integer> getGlobalFrequenciesByLemmas(List<String> lemmas) {
        List<GlobalLemmaFrequencyProjection> results = lemmaRepository.findGlobalFrequenciesByLemmas(lemmas);

        return results.stream()
                .collect(Collectors.toMap(
                        GlobalLemmaFrequencyProjection::getLemma,
                        projection -> projection.getTotalFrequency().intValue()
                ));
    }
}
