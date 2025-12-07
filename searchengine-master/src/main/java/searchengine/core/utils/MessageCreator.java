package searchengine.core.utils;

import lombok.experimental.UtilityClass;

import java.text.MessageFormat;
import java.util.Arrays;

@UtilityClass
public class MessageCreator {

    public static String createMessage(Object[] args) {
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
