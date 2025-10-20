package searchengine.exceptions;

import java.text.MessageFormat;

public class NoFoundEntityException extends RuntimeException {
  public NoFoundEntityException(Object ... args) {
    super(createMessage(args));
  }

  private static String createMessage(Object[] args) {
    return MessageFormat.format("Ошибка {0} для {1}: {2}", args);
  }
}
