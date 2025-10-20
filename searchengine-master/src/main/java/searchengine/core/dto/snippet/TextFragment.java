package searchengine.core.dto.snippet;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class TextFragment {
    private int start;
    private int end;
    private String originalText;
    private List<WordPosition> words;

    public TextFragment(int start, int end, String originalText){
        this.start = start;
        this.end = end;
        this.originalText = originalText;
        words = new ArrayList<>();
    }

    public void addWord(WordPosition word){
        words.add(word);
    }
}
