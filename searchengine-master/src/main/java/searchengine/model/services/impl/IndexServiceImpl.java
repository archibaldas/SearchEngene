package searchengine.model.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.core.utils.BeanUtils;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;
import searchengine.model.entity.SiteEntity;
import searchengine.model.projection.GlobalSearchProjection;
import searchengine.model.projection.PageRankProjection;
import searchengine.model.repositories.SearchIndexRepository;
import searchengine.model.services.IndexService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static searchengine.core.utils.MathUtils.getRankForPage;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    private final SearchIndexRepository indexRepository;

    @Override
    public List<SearchIndex> findAllByPage(Page page) {
        return indexRepository.findAllByPage(page);
    }

    @Override
    public SearchIndex findById(Long id) {
        return indexRepository.findById(id)
                .orElseThrow(() -> new NoFoundEntityException("Индекс c", id, " в базе данных не найден."));
    }

    @Override
    public List<SearchIndex> findByLemma(Lemma lemma) {
        return indexRepository.findByLemma(lemma);
    }

    @Override
    public int count() {
        return Math.toIntExact(indexRepository.count());
    }

    @Override
    @Transactional
    public SearchIndex update(SearchIndex entity) {
        SearchIndex updatedIndex = findById(entity.getId());
        BeanUtils.copyNotNullProperties(entity, updatedIndex);
        return indexRepository.save(updatedIndex);
    }

    @Override
    @Transactional
    public void deleteAllByList(List<SearchIndex> indexes) {
        indexRepository.deleteAll(indexes);
    }

    @Override
    public void createIndexList(Page page, List<Lemma> lemmas, Map<String, Integer> lemmaNap) {
        List<SearchIndex> indexesToSave = new ArrayList<>();
        Integer allLemmasCount = lemmaNap.values().stream().mapToInt(Integer :: intValue).sum();
        for (Lemma savedLemma : lemmas) {
            String dbLemmaText = savedLemma.getLemma();


            Integer lemmaCount = lemmaNap.get(dbLemmaText);
            if (lemmaCount == null) {
                log.warn("Лемма '{}' из БД не найдена в карте страницы. Пропуск индексации.", dbLemmaText);
                continue;
            }
            float rank = getRankForPage(lemmaCount, allLemmasCount);

            SearchIndex index = new SearchIndex();
            index.setPage(page);
            index.setLemma(savedLemma);
            index.setRank(rank);
            indexesToSave.add(index);
        }
        indexRepository.saveAllAndFlush(indexesToSave);
    }

    @Override
    public List<Page> findPagesByAllLemmas(List<String> lemmas, SiteEntity site, long lemmaCount) {
        return indexRepository.findPagesByAllLemmas(lemmas,site, lemmaCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Page> findPagesByAllLemmasGlobal(List<String> lemmas, long lemmaCount) {
        List<GlobalSearchProjection> results = indexRepository.findPagesByAllLemmasGlobal(lemmas, lemmaCount);
        return results.stream()
                .map(GlobalSearchProjection::getPage)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Page,Float> findPagesByAllLemmasGlobalWithRank(List<String> lemmas, long lemmaCount) {
        List<GlobalSearchProjection> results = indexRepository.findPagesByAllLemmasGlobal(lemmas, lemmaCount);
        return results.stream().collect(Collectors.toMap(
                GlobalSearchProjection::getPage,
                projection -> projection.getTotalRank().floatValue()
                )
        );
    }

    @Override
    public Map<Page, Float> findPagesWithAbsRel(List<Page> pages, List<String> lemmas) {
        List<PageRankProjection> results = indexRepository.findPagesRanks(pages, lemmas);
        return results.stream().collect(Collectors.toMap(
                PageRankProjection::getPage,
                projection -> projection.getTotalRank().floatValue()
        ));
    }
}
