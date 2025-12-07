package searchengine.exceptions;

import static searchengine.core.utils.MessageCreator.createMessage;

public class NoFoundEntityException extends RuntimeException {
  public NoFoundEntityException(Object ... args) {
    super(createMessage(args));
  }
}
