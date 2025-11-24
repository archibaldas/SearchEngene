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

    private static final Pattern RUSSIAN_WORDS_PATTERN = Pattern.compile("[а-яё]+",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private final LuceneMorphology luceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] PARTICLE_TYPES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС", "МС-П" };


    public Map<String, Integer> getLemmasMapFromPageContent(String pageContent){
        String textFromPage = HtmlUtils.getCleanTextFromContent(pageContent);
        String[] wordsArray = extractRussianWords(textFromPage);
        Map<String, Integer> lemmasMap = new HashMap<>();
        if (wordsArray.length == 0) {
            throw new NoFoundRussianContentException("На странице не найдено российских слов");
        }
        for(String word : wordsArray){
            if(word.isEmpty() || !isCorrectWordForm(word)) continue;

            List<String> wordBaseForm = luceneMorphology.getMorphInfo(word);
            if(anyWordBaseBelongToParticle(wordBaseForm)) continue;

            List<String> lemmas = luceneMorphology.getNormalForms(word);
            for(String lemma : lemmas){
                lemmasMap.merge(normalizeLemma(lemma), 1 , Integer :: sum);
            }
        }
        return lemmasMap;
    }

    public Set<String> getLemmasSetFromSearch(String text){
        return getLemmasMapFromPageContent(text).keySet();
    }

    private boolean isCorrectWordForm(String word){
        return luceneMorphology.getMorphInfo(word).stream()
                .noneMatch(info -> info.matches(WORD_TYPE_REGEX));
    }

    private static String[] extractRussianWords(String text){
        return RUSSIAN_WORDS_PATTERN.matcher(text.toLowerCase())
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
    }

    private static boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(LemmaFinder::isParticle);
    }

    private static boolean isParticle(String wordBaseForm){
        return Arrays.stream(PARTICLE_TYPES)
                .anyMatch(wordBaseForm.toUpperCase()::contains);
    }

    private String normalizeLemma(String lemma) {
        return lemma.toLowerCase().replace('ё', 'е');
    }
}
