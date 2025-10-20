package searchengine.core.dto.snippet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordPosition {
    private int start;
    private int end;
    private String lemma;
    private String fullWord;
}
