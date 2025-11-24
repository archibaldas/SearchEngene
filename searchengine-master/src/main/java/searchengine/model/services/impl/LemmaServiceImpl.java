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
import searchengine.model.repositories.LemmaRepository;
import searchengine.model.repositories.SiteEntityRepository;
import searchengine.model.services.LemmaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {

    private final SiteEntityRepository siteEntityRepository;
    private final JdbcTemplate jdbcTemplate;

    private final LemmaRepository lemmaRepository;

    @Override
    public List<Lemma> findAllBySite(SiteEntity siteEntity) {
        return lemmaRepository.findAllBySite(siteEntity);
    }

    @Override
    public Lemma findById(Long id) {
        return lemmaRepository.findById(id)
                .orElseThrow(() -> new NoFoundEntityException("поиска леммы", id, "Лемма по ID не найдена."));
    }

    @Override
    public Lemma findByLemmaAndSite(String lemma, SiteEntity site) {
        return lemmaRepository.findByLemmaAndSite(lemma, site)
                .orElseThrow(() -> new NoFoundEntityException("поиска леммы: " + lemma,
                        site.getUrl(), "Лемма не найдена в базе данных"));
    }

    @Override
    public List<Lemma> findByLemma(String lemma) {
        return lemmaRepository.findByLemma(lemma);
    }

    @Override
    @Transactional
    public void updateLemmasForSite(SiteEntity site, Map<String, Integer> lemmasOnPage) {
        if(lemmasOnPage.isEmpty()) return;

        String sql = "INSERT INTO lemma (site_id, lemma, frequency) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE frequency = frequency + 1";

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
    public Lemma create(Lemma entity) {
        try {
            return lemmaRepository.save(entity);
        } catch (Exception e) {
            log.debug(e.getMessage());
            Lemma lemma = findByLemmaAndSite(entity.getLemma(), entity.getSite());
            BeanUtils.copyNotNullProperties(entity, lemma);
            lemma.setFrequency(lemma.getFrequency() + 1);
            return lemmaRepository.save(lemma);
        }
    }

    @Override
    @Transactional
    public Lemma update(Lemma entity) {
        Lemma updatedLemma = findById(entity.getId());
        BeanUtils.copyNotNullProperties(entity, updatedLemma);
        return lemmaRepository.save(updatedLemma);
    }

    @Override
    @Transactional
    public void delete(Lemma entity) {
        lemmaRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteAllByList(List<Lemma> lemmas) {
        lemmaRepository.deleteAll(lemmas);

    }
}
