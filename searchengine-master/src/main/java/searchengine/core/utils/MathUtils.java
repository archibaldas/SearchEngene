package searchengine.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import searchengine.model.entity.Page;

import java.util.Map;

@UtilityClass
@Slf4j
public class MathUtils {

    private static final float TO_PERCENTAGE = 100.0f;

    public static double getThreshold(long pageCount){
        return pageCount * 0.8;
    }

    public static float getMaxRel(Map<Page, Float> absRelMap) {
        return absRelMap.values().stream()
                .max(Float::compare)
                .orElse(1f);
    }

    public static float getRankForPage(Integer lemmasCount, Integer allLemmasCount){
        return Math.round(((float) lemmasCount / allLemmasCount) * 10000) / TO_PERCENTAGE;
    }
}
