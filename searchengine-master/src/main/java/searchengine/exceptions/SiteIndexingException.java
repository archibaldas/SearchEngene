package searchengine.exceptions;

import static searchengine.core.utils.MessageCreator.createMessage;

public class SiteIndexingException extends RuntimeException {
    public SiteIndexingException(Object ... args) {
        super(createMessage(args));
    }
}
