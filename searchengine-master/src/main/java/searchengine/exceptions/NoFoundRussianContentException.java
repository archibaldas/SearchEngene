package searchengine.exceptions;

import java.text.MessageFormat;
import java.util.Arrays;

public class NoFoundRussianContentException extends RuntimeException {

  public NoFoundRussianContentException(Object... args) {
    super(createMessage(args));
  }

  private static String createMessage(Object[] args) {
    return MessageFormat.format(patternGenerator(args), args);
  }

  private static String patternGenerator(Object[] args) {
    StringBuilder pattern = new StringBuilder();
    int argsSize = Math.toIntExact(Arrays.stream(args).count());
    for (int i = 0; i < argsSize; i++) {
      pattern.append("{").append(i).append("}");
    }
    return pattern.toString();
  }
}
