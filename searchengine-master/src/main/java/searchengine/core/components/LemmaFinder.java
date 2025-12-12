package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.stereotype.Component;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.NoFoundRussianContentException;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class LemmaFinder {

    private final Pattern russianWordPattern;
    private final LuceneMorphology luceneMorphology;
    private final String wordTypeRegex;
    private final List<String> particleTypes;

    public Map<String, Integer> getLemmasMapFromPageContent(String pageContent) throws NoFoundRussianContentException{
        String textFromPage = HtmlUtils.getCleanTextFromContent(pageContent);
        Map<String, Integer> lemmasMap = new HashMap<>();
        String[] wordsArray = getWordsArray(textFromPage);

        for(String word : wordsArray){
            List<String> lemmas = getLemmasFromWord(word);
            for(String lemma : lemmas){
                lemmasMap.merge(normalizeLemma(lemma), 1 , Integer :: sum);
            }
        }
        return lemmasMap;
    }

    public Set<String> getLemmasSetFromSearch(String text) throws NoFoundRussianContentException{
        return getLemmasMapFromPageContent(text).keySet();
    }

    public Map<String, Set<String>> getLemmasMapWithWordFromText(String text) throws NoFoundRussianContentException{
        String[] wordsArray = getWordsArray(text);
        Map<String, Set<String>> lemmasMapWithWordsFromText = new HashMap<>();
        for (String word : wordsArray) {
            List<String> lemmas = getLemmasFromWord(word);

            if (lemmas.isEmpty()) continue;

            lemmas.stream().map(this::normalizeLemma)
                    .forEach(l -> lemmasMapWithWordsFromText
                            .computeIfAbsent(l, k -> new HashSet<>()).add(word));

        }
        return lemmasMapWithWordsFromText;
    }

    private List<String> getLemmasFromWord(String word){
        if(!word.isEmpty() || isCorrectWordForm(word)){
            List<String> wordBaseForm = luceneMorphology.getMorphInfo(word);
            if(!anyWordBaseBelongToParticle(wordBaseForm)){
                return luceneMorphology.getNormalForms(word);
            }
        }
        return List.of();
    }

    private String[] getWordsArray(String text){
        String[] wordsArray = extractRussianWords(text);
        if(wordsArray.length == 0 ){
            throw new NoFoundRussianContentException("На странице не найдено российских слов");
        }
        return wordsArray;
    }

    private String[] extractRussianWords(String text){
        return russianWordPattern.matcher(text.toLowerCase())
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
    }

    private boolean isCorrectWordForm(String word){
        return luceneMorphology.getMorphInfo(word).stream()
                .noneMatch(info -> info.matches(wordTypeRegex));
    }


    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::isParticle);
    }

    private boolean isParticle(String wordBaseForm){
        return particleTypes.stream().anyMatch(wordBaseForm.toUpperCase()::contains);
    }

    private String normalizeLemma(String lemma) {
        return lemma.toLowerCase().replace('ё', 'е');
    }
}
