package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.core.dto.snippet.TextFragment;
import searchengine.core.dto.snippet.WordBoundaries;
import searchengine.core.dto.snippet.WordPosition;
import searchengine.core.utils.HtmlUtils;
import searchengine.core.utils.MathUtils;
import searchengine.model.entity.Page;
import searchengine.services.dto.responses.SearchResult;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResponseBuilder {

    private final MathUtils mathUtils;

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

        return new SearchResult(
                page.getSite().getUrl(),
                page.getSite().getName(),
                page.getPath(),
                HtmlUtils.getTitleFromHtml(page.getContent()),
                buildMultiSnippet(content, lemmaStrings),
                rel);
    }

    private String buildMultiSnippet(String content, Set<String> lemmaStrings){
        if(lemmaStrings.isEmpty()) {
            return getEnhancedFallbackSnippet(content, lemmaStrings);
        }

        List<WordPosition> allPositions = findAllWordPositions(content, lemmaStrings);

        if(allPositions.isEmpty()){
            return getEnhancedFallbackSnippet(content, lemmaStrings);
        }

        List<TextFragment> fragments = groupPositionsIntoFragments(content, allPositions);

        List<TextFragment> bestFragments = selectBestFragments(fragments, 3);

        return buildSnippetFromFragments(bestFragments, content, lemmaStrings);
    }

    private String getEnhancedFallbackSnippet(String content, Set<String> lemmaStrings) {
        if(!lemmaStrings.isEmpty()) {
            for(String lemma : lemmaStrings) {
                int index = content.toLowerCase().indexOf(lemma.toLowerCase());
                if(index != -1){
                    int start = Math.max(0,index - 100);
                    int end = Math.min(content.length(), index + 100);
                    String snippet = content.substring(start, end);
                    return highlightPartialMatches(snippet, lemmaStrings) + "...";
                }
            }
        }
        return content.length() > 250 ? content.substring(0, 250) + "..." : content;
    }

    private String highlightPartialMatches(String snippet, Set<String> lemmaStrings) {
        String result = snippet;

        for(String lemma : lemmaStrings){
            Pattern pattern = Pattern.compile("(?i)(\\p{L}*" + Pattern.quote(lemma) + "\\p{L}*)");
            Matcher matcher = pattern.matcher(result);

            StringBuilder sb = new StringBuilder();

            while(matcher.find()) {
                String foundWord = matcher.group(1);
                matcher.appendReplacement(sb, "<b>" + foundWord +  "</b>");
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    private String buildSnippetFromFragments(List<TextFragment> fragments, String content, Set<String> lemmaStrings) {
        if(fragments.isEmpty()){
            return getEnhancedFallbackSnippet(content, lemmaStrings);
        }

        StringBuilder snippet = new StringBuilder();

        for( int i = 0; i < fragments.size(); i++) {
            TextFragment fragment = fragments.get(i);

            String highlightedFragment = highlightWordsInFragment(fragment, lemmaStrings);
            snippet.append(highlightedFragment);

            if (i < fragments.size() - 1){
                snippet.append("...");
            }
        }

        return snippet.toString();
    }

    private String highlightWordsInFragment(TextFragment fragment, Set<String> lemmaStrings) {
        String fragmentText = fragment.getOriginalText();

        for(WordPosition wordPos : fragment.getWords()){
            String fullWord = wordPos.getFullWord();
            fragmentText = highlightWordInText(fragmentText, fullWord);
        }
        return fragmentText;
    }

    private String highlightWordInText(String text, String word) {
        Pattern pattern = Pattern.compile("(?i)(\\p{L}*" + Pattern.quote(word) + "\\p{L}*)");
        Matcher matcher = pattern.matcher(text);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<b>" + matcher.group() + "</b>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }


    private List<TextFragment> selectBestFragments(List<TextFragment> fragments, int maxFragments) {
        return fragments.stream()
                .sorted(Comparator.comparingInt((TextFragment f) -> f.getWords().size()).reversed())
                .limit(maxFragments)
                .sorted(Comparator.comparingInt(TextFragment::getStart))
                .toList();
    }

    private List<WordPosition> findAllWordPositions(String content, Set<String> lemmaStrings){
        List<WordPosition> positions = new ArrayList<>();
        String lowerContent = content.toLowerCase();

        for(String lemma : lemmaStrings) {
            int index = 0;
            while((index = lowerContent.indexOf(lemma.toLowerCase(), index)) != -1){
                WordBoundaries boundaries = findWordBoundaries(content, index, lemma.length());
                positions.add(new WordPosition(
                        boundaries.getStart(),
                        boundaries.getEnd(),
                        lemma,
                        content.substring(boundaries.getStart(), boundaries.getEnd())
                ));
                index = boundaries.getEnd();
            }
        }

        positions.sort(Comparator.comparingInt(WordPosition::getStart));
        return positions;
    }

    private WordBoundaries findWordBoundaries(String content, int lemmaStart, int lemmaLength){
        int wordStart = lemmaStart;
        int wordEnd = lemmaStart + lemmaLength;

        while (wordStart > 0 && Character.isLetter(content.charAt(wordStart - 1))){
            wordStart --;
        }

        while (wordEnd < content.length() && Character.isLetter(content.charAt(wordEnd))){
            wordEnd ++;
        }

        return new WordBoundaries(wordStart, wordEnd);
    }

    private List< TextFragment> groupPositionsIntoFragments(String content, List<WordPosition> positions){
        List<TextFragment> fragments = new ArrayList<>();
        int fragmentRadius = 80;

        for(WordPosition position : positions){
            int fragmentStart = Math.max(0, position.getStart() - fragmentRadius);
            int fragmentEnd = Math.min(content.length(), position.getEnd() + fragmentRadius);

            String fragmentText = content.substring(fragmentStart, fragmentEnd);
            TextFragment fragment = new TextFragment(fragmentStart, fragmentEnd, fragmentText);

            fragment.addWord(position);
            fragments.add(fragment);
        }

        return mergeOverlappingFragments(fragments);
    }

    private List<TextFragment> mergeOverlappingFragments(List<TextFragment> fragments) {
        if(fragments.isEmpty()){
            return fragments;
        }

        List<TextFragment> merged = new ArrayList<>();
        fragments.sort(Comparator.comparingInt(TextFragment::getStart));

        TextFragment current = fragments.get(0);

        for (int i = 1 ; i < fragments.size(); i++){
            TextFragment next = fragments.get(i);
            if(current.getEnd() >= next.getStart()){
                current = mergeTwoFragments(current, next);
            } else {
                merged.add(current);
                current = next;
            }

        }
        merged.add(current);
        return merged;
    }

    private TextFragment mergeTwoFragments(TextFragment first, TextFragment second) {
        int start = Math.min(first.getStart(), second.getStart());
        int end = Math.max(first.getEnd(), second.getEnd());
        String mergedText = first.getOriginalText() + "..." + second.getOriginalText();

        TextFragment merged = new TextFragment(start, end, mergedText);
        merged.getWords().addAll(first.getWords());
        merged.getWords().addAll(second.getWords());

        return merged;
    }
}
