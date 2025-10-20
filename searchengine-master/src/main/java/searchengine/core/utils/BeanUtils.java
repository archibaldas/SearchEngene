package searchengine.core.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@UtilityClass
@Slf4j
public class BeanUtils {

    @SneakyThrows
    public void copyNotNullProperties(Object source, Object destination){
        Class<?> clazz = source.getClass();

        while (clazz != null) {
            for(Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(source);
                if(value != null){
                    try {
                        Field targetField = destination.getClass().getDeclaredField(field.getName());
                        targetField.setAccessible(true);
                        targetField.set(destination, value);
                    } catch (NoSuchFieldException ignored){
                        log.debug("Пропуск поля без назначения");
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
