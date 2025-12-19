package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.exceptions.SearchingException;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;
import searchengine.model.services.IndexService;
import searchengine.model.services.LemmaService;
import searchengine.model.services.PageService;
import searchengine.model.services.SiteService;

import java.util.*;
import java.util.stream.Collectors;

import static searchengine.core.utils.HtmlUtils.normalizeUrl;
import static searchengine.core.utils.MathUtils.getThreshold;

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
        site = siteUrl.isEmpty() ? null : siteService.findByUrl(normalizeUrl(siteUrl));
        List<String> sortedLemmas = getSortedLemmas(lemmasStrings);
        if(sortedLemmas.isEmpty()) return new ArrayList<>();
        return site != null ? indexService.findPagesByAllLemmas(sortedLemmas, site, sortedLemmas.size())
                : indexService.findPagesByAllLemmasGlobal(sortedLemmas, sortedLemmas.size());
    }

    private List<String> getSortedLemmas(Set<String> lemmasString){
        Map<String, Integer> lemmaTotalFrequency = getLemmaTotalFrequenceMap(lemmasString);
        calculateThreshold();
        if (lemmaTotalFrequency.isEmpty()) throw new SearchingException("Запрос не корректен, или вы используете слова не из морфологии русского языка");
        return lemmasString.stream()
                .filter(lemma -> lemmaTotalFrequency.get(lemma) < threshold)
                .sorted(Comparator.comparingInt(lemmaTotalFrequency::get))
                .toList();
    }

    private void calculateThreshold(){
        if (site != null) {
            threshold = getThreshold(pageService.countBySite(site));
        } else {
            threshold = getThreshold(pageService.count());
        }
    }

    public Map<Page, Float> getAbsRelMap(List<Page> pages, Set<String> searchLemmas){
        return indexService.findPagesWithAbsRel(pages, searchLemmas.stream().toList());
    }

    private Map<String, Integer> getLemmaTotalFrequenceMap(Set<String> lemmasString){
        Map<String, Integer> lemmaTotalFrequency = site == null
                ? lemmaService.getGlobalFrequenciesByLemmas(lemmasString.stream().toList())
                : lemmaService.getFrequenciesBySiteAndLemmas(site, lemmasString.stream().toList());
        if(lemmasString.size() != lemmaTotalFrequency.size()){
            Set<String> nullFrequencyLemma = lemmasString.stream().filter(l -> !lemmaTotalFrequency.containsKey(l))
                    .collect(Collectors.toSet());
            nullFrequencyLemma.forEach(l -> lemmaTotalFrequency.put(l, 0));
        }
        return lemmaTotalFrequency;
    }
}
