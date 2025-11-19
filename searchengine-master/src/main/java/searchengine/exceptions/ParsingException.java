package searchengine.exceptions;

import java.text.MessageFormat;


public class ParsingException extends RuntimeException {

  public ParsingException (String operation, String url, Throwable cause){
    super(createMessage(operation, url, cause), cause);
  }

  private static String createMessage(String operation, String url, Throwable cause) {
    return MessageFormat.format("Ошибка {0} для {1}: {2}", operation, url, cause.getMessage());
  }
}
