package searchengine.exceptions;

import static searchengine.core.utils.MessageCreator.createMessage;

public class PageIndexingException extends RuntimeException {
    public PageIndexingException(Object ... args) {
        super("Ошибка индексации страницы: " + createMessage(args));
    }
}
