package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.model.entity.Lemma;
import searchengine.model.entity.Page;
import searchengine.model.entity.SearchIndex;
import searchengine.model.entity.SiteEntity;
import searchengine.model.services.IndexService;
import searchengine.model.services.LemmaService;
import searchengine.model.services.PageService;
import searchengine.model.services.SiteService;

import java.util.*;
import java.util.stream.Collectors;

import static searchengine.core.utils.MathUtils.getTrashHold;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchingUtils {

    private final PageService pageService;

    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final SiteService siteService;
    private SiteEntity site;

    private double threshold;

    public List<Page> getMatchingPages(Set<String> lemmasStrings,
                                       String siteUrl) {
        List<String> sortedLemmas = getSortedLemmas(lemmasStrings, siteUrl);
        List<Page> matchingPages = findPagesByLemma(sortedLemmas.get(0));

        for(int i = 1; i < sortedLemmas.size() && !matchingPages.isEmpty(); i++ ){
            String currentLemma = sortedLemmas.get(i);
            List<Page> pagesWithLemma = findPagesByLemma(currentLemma);
            Set<Page> pageSet = new HashSet<>(pagesWithLemma);

            matchingPages = matchingPages.stream()
                    .filter(pageSet::contains)
                    .toList();
        }
        return matchingPages;
    }

    private List<Page> findPagesByLemma(String lemma) {
        if(site != null) {
            try {
                return indexService.findByLemma(lemmaService.findByLemmaAndSite(lemma, site)).stream()
                        .map(SearchIndex :: getPage)
                        .toList();
            }catch (NoFoundEntityException e) {
                return new ArrayList<>();
            }
        }
        return lemmaService.findByLemma(lemma).stream()
                .flatMap(l -> indexService.findByLemma(l).stream())
                .map(SearchIndex::getPage)
                .distinct()
                .toList();
    }

    private List<String> getSortedLemmas(Set<String> lemmasString, String siteUrl){
        Map<String, Integer> lemmaTotalFrequency = siteUrl.isEmpty()
                ? calculateTotalFrequency(lemmasString) : calculateTotalFrequency(lemmasString, siteUrl);
        calculateThreshold();
        return lemmasString.stream()
                .filter(lemma -> lemmaTotalFrequency.get(lemma) < threshold)
                .sorted(Comparator.comparingInt(lemmaTotalFrequency::get))
                .toList();
    }

    private Map<String, Integer> calculateTotalFrequency(Set<String> lemmaStrings){
        return lemmaStrings.stream()
                .collect(Collectors.toMap(
                        lemma -> lemma,
                        lemma -> {
                            List<Lemma> lemmas = lemmaService.findByLemma(lemma);
                            return lemmas.stream()
                                    .mapToInt(Lemma :: getFrequency)
                                    .sum();
                        }
                ));
    }

    private Map<String, Integer> calculateTotalFrequency(Set<String> lemmaStrings, String siteUrl){
        site = siteService.findByUrl(siteUrl);
        return lemmaStrings.stream()
                .collect(Collectors.toMap(
                        lemma -> lemma,
                        lemma -> {
                            try {
                                Lemma foundLemma = lemmaService.findByLemmaAndSite(lemma, site);
                                return foundLemma.getFrequency();
                            } catch (NoFoundEntityException e){
                                return Integer.MAX_VALUE;
                            }
                        }
                ));
    }


    private void calculateThreshold(){
        if (site != null) {
            threshold = getTrashHold(pageService.countBySite(site));
        } else {
            threshold = getTrashHold(pageService.count());
        }
    }

    private List<SearchIndex> findIndexesForPageAndLemma(Page page, String lemma){
        List<Lemma> lemmas = lemmaService.findByLemma(lemma);
        if(lemmas.isEmpty()) return new ArrayList<>();
        if(lemmas.size() == 1) {
            return indexService.findByLemma(lemmas.get(0)).stream()
                    .filter(i -> i.getPage().equals(page))
                    .toList();
        }
        return lemmas.stream()
                .flatMap(l -> indexService.findByLemma(l).stream())
                .filter(i -> i.getPage().equals(page))
                .toList();
    }

    public Map<Page, Float> getAbsRelMap(List<Page> pages, Set<String> searchLemmas){
        Map<Page, Float> absRelMap = new HashMap<>();

        for(Page page : pages){
            float absoluteRelevance = 0f;
            for(String lemma : searchLemmas){
                List<SearchIndex> indexes = findIndexesForPageAndLemma(page, lemma);
                for(SearchIndex index : indexes) {
                    absoluteRelevance += index.getRank();
                }
            }
            absRelMap.put(page, absoluteRelevance);
        }
        return absRelMap;
    }
}
