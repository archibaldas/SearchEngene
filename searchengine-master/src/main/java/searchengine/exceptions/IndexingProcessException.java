package searchengine.exceptions;

import static searchengine.core.utils.MessageCreator.createMessage;

public class IndexingProcessException extends RuntimeException {
    public IndexingProcessException(Object ... args) {
        super(createMessage(args));
    }
}
