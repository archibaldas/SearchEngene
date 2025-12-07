package searchengine.exceptions;

import static searchengine.core.utils.MessageCreator.createMessage;

public class NoFoundRussianContentException extends RuntimeException {

  public NoFoundRussianContentException(Object... args) {
    super(createMessage(args));
  }


}
