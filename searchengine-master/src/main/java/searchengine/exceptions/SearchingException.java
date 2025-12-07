package searchengine.exceptions;

import static searchengine.core.utils.MessageCreator.createMessage;

public class SearchingException extends RuntimeException {
    public SearchingException(Object ... args) {
        super(createMessage(args));
    }
}
