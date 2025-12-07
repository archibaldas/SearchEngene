package searchengine.config;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Configuration
@ConfigurationProperties(prefix = "app.lemma-finder")
public class LemmaFinderConfig {

    @Bean
    public LuceneMorphology luceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.lemma-finder")
    public List<String> particleTypes() {
        return Arrays.asList("МЕЖД", "ПРЕДЛ", "СОЮЗ", "МС", "МС-П");
    }

    @Bean
    public Pattern russianWordPattern(){
        return Pattern.compile("[а-яё]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Bean
    public String wordTypeRegex(){
        return "\\W\\w&&[^а-яА-Я\\s]";
    }
}
