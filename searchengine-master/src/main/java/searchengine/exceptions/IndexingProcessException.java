package searchengine.exceptions;

import java.text.MessageFormat;

public class IndexingProcessException extends RuntimeException {
    public IndexingProcessException(String statusIndexing) {
        super(createMessage(statusIndexing));
    }

    private static String createMessage(String statusIndexing){
      return MessageFormat.format("Индексация {0} запущена.", statusIndexing);
    }
}
