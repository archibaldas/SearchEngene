package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.core.utils.HtmlUtils;
import searchengine.core.utils.MathUtils;
import searchengine.model.entity.Page;
import searchengine.web.services.dto.responses.SearchResult;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResponseBuilder {

    private final LemmaFinder lemmaFinder;
    private static final int MAX_SNIPPET_LENGTH = 250;

    public List<SearchResult> buildSearchResultList(Map<Page, Float> absRelMap,
                                                    int offset,
                                                    int limit,
                                                    Set<String> lemmaStrings) {
        float maxRel = MathUtils.getMaxRel(absRelMap);
        return absRelMap.entrySet().stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .skip(offset)
                .limit(limit)
                .map(entry -> buildSearchResult(entry, maxRel, lemmaStrings))
                .toList();
    }

    private SearchResult buildSearchResult(Map.Entry<Page,Float> entry,
                                           float maxRel, Set<String> lemmaStrings){
        Page page = entry.getKey();
        float rel = maxRel > 0 ? entry.getValue() / maxRel : 0f;

        String content = Jsoup.parse(page.getContent()).text();
        String text = HtmlUtils.getCleanTextFromContent(content);

        return new SearchResult(
                page.getSite().getUrl(),
                page.getSite().getName(),
                page.getPath(),
                HtmlUtils.getTitleFromHtml(page.getContent()),
                buildSnippet(text, lemmaStrings),
                rel);
    }

    private String buildSnippet(String text, Set<String> searchLemmas) {
        if (isInvalidInput(text, searchLemmas)) {
            return getFallbackSnippet(text);
        }

        Set<String> wordsToFind = findWordsForLemmas(text, searchLemmas);
        if (wordsToFind.isEmpty()) {
            return getFallbackSnippet(text);
        }

        int[] bounds = findBestSnippetBounds(text, wordsToFind);
        String snippet = extractAndHighlightSnippet(text, bounds, wordsToFind);

        return addEllipsis(snippet, bounds, text.length());
    }

    private boolean isInvalidInput(String text, Set<String> searchLemmas) {
        return text == null || text.isEmpty() ||
                searchLemmas == null || searchLemmas.isEmpty();
    }

    private int[] findBestSnippetBounds(String text, Set<String> wordsToFind) {
        int bestStart = 0;
        int bestEnd = MAX_SNIPPET_LENGTH;
        int maxCount = 0;

        for (int start = 0; start <= text.length() - MAX_SNIPPET_LENGTH; start += 50) {
            int end = Math.min(start + MAX_SNIPPET_LENGTH, text.length());
            int count = countWordsInWindow(text.substring(start, end), wordsToFind);

            if (count > maxCount) {
                maxCount = count;
                bestStart = start;
                bestEnd = end;
            }
        }

        return new int[]{bestStart, bestEnd};
    }

    private String extractAndHighlightSnippet(String text, int[] bounds, Set<String> wordsToFind) {
        String snippet = text.substring(bounds[0], bounds[1]);
        return highlightPartialMatches(snippet, wordsToFind);
    }

    private String addEllipsis(String snippet, int[] bounds, int textLength) {
        if (bounds[0] > 0) snippet = "..." + snippet;
        if (bounds[1] < textLength) snippet = snippet + "...";
        return snippet;
    }

    private String highlightPartialMatches(String snippet, Set<String> lemmaWords) {
        StringBuilder sb = new StringBuilder();

        String[] snippetArray = snippet.split(" ");
        for(String snippetWord : snippetArray){
            for(String lemmaWord : lemmaWords){
                if(snippetWord.replaceAll("[.,!?;:\"'«»]", "").equalsIgnoreCase(lemmaWord)){
                    snippetWord = "<b>%s</b>".formatted(snippetWord);
                    break;
                }
            }
            sb.append(snippetWord).append(" ");
        }
        return sb.toString().replaceAll("</b>\\s+<b>", " ");
    }

    private String getFallbackSnippet(String text) {
        return text.length() > MAX_SNIPPET_LENGTH ?
                text.substring(0, MAX_SNIPPET_LENGTH) + "..." : text;
    }

    private int countWordsInWindow(String window, Set<String> wordsToFind) {
        String lowerWindow = window.toLowerCase();
        int count = 0;

        for (String word : wordsToFind) {
            if (lowerWindow.contains(word.toLowerCase())) {
                count++;
            }
        }

        return count;
    }

    private Set<String> findWordsForLemmas(String text, Set<String> searchLemmas) {
        Map<String, Set<String>> lemmasMap = lemmaFinder.getLemmasMapWithWordFromText(text);
        Set<String> wordsToFind = new HashSet<>();

        for (String lemma : searchLemmas) {
            Set<String> words = lemmasMap.get(lemma);
            if (words != null) {
                wordsToFind.addAll(words);
            }
        }

        return wordsToFind;
    }
}
