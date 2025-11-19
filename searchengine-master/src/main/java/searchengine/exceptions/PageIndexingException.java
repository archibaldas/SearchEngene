package searchengine.exceptions;

import java.text.MessageFormat;

public class PageIndexingException extends RuntimeException {
    public PageIndexingException(String process, String errorMessage, String link) {
        super(createMessage(process, errorMessage, link));
    }

    private static String createMessage(String process, String errorMessage, String link) {
        return MessageFormat.format("Ошибка {0}: {1}: Ссылка на страницу: [{2}]", process, errorMessage, link);
    }
}
