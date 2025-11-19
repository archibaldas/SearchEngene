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
import searchengine.model.repositories.SearchIndexRepository;
import searchengine.model.services.IndexService;

import java.util.List;

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
                .orElseThrow(() -> new NoFoundEntityException("поиска индекса по ID", id, "Индекс в базе данных не найден."));
    }

    @Override
    public SearchIndex findByLemmaAndPage(Lemma lemma, Page page) {
        return indexRepository.findByLemmaAndPage(lemma, page)
                .orElseThrow(() -> new NoFoundEntityException("поиска индекса по лемме: " + lemma.getLemma(),
                        page.getSite().getUrl() + page.getPath(), "Индекс в базе данных не найден."));
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
    public SearchIndex create(SearchIndex entity) {
        return indexRepository.save(entity);
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
    public void delete(SearchIndex entity) {
        indexRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteAllByList(List<SearchIndex> indexes) {
        indexRepository.deleteAll(indexes);
    }
}
