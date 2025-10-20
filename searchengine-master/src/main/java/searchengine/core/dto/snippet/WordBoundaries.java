package searchengine.core.dto.snippet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordBoundaries {
    private int start;
    private int end;
}
