package searchengine.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.entity.Page;

import java.util.Map;

@UtilityClass
@Slf4j
public class MathUtils {

    public static double getTrashHold(long pageCount){
        return pageCount * 0.8;
    }

    public static float getMaxRel(Map<Page, Float> absRelMap) {
        return absRelMap.values().stream()
                .max(Float::compare)
                .orElse(1f);
    }
}
