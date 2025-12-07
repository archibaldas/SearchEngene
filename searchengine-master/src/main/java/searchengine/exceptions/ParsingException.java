package searchengine.exceptions;

import static searchengine.core.utils.MessageCreator.createMessage;


public class ParsingException extends RuntimeException {

  public ParsingException (Object ... args){
    super(createMessage(args));
  }
}
