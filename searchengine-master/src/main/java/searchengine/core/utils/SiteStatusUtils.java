package searchengine.core.utils;

import lombok.experimental.UtilityClass;
import searchengine.model.entity.SiteEntity;
import searchengine.model.entity.StatusType;

@UtilityClass
public class SiteStatusUtils {

    public static final String STOP_INDEXING = "Индексация остановлена пользователем";

    public static SiteEntity setIndexingProcessStatus(SiteEntity siteEntity){
        if(siteEntity != null) siteEntity.setStatus(StatusType.INDEXING);
        return siteEntity;
    }

    public static SiteEntity setIndexedStatus(SiteEntity siteEntity){
        if(siteEntity != null) siteEntity.setStatus(StatusType.INDEXED);
        return siteEntity;
    }

    public static SiteEntity setFailedStatus(SiteEntity siteEntity, String error){
        if(siteEntity != null){
            siteEntity.setStatus(StatusType.FAILED);
            siteEntity.setLastError(!error.isEmpty() ? error : "Неизвестная ошибка индексации сайта");
        }
        return siteEntity;
    }
}
