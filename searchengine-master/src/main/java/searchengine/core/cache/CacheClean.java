package searchengine.core.cache;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import searchengine.model.services.PageService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheClean {
    @Setter
    private AtomicBoolean indexingRunning;
    private final UrlCache urlCache;
    private final PageService pageService;

    @Scheduled(fixedRateString = "${app.cache.remove_time}")
    public void removeFoundedUrls(){
        if(indexingRunning != null && indexingRunning.get()){
            int before = urlCache.getCache().size();

            Set<String> urlsToCheck = new HashSet<>(urlCache.getCache());

            for(String url : urlsToCheck){
                if(pageService.existsPageLinkInDatabase(url)){
                    urlCache.removeIfExists(url);
                }
            }

            int removed = before - urlCache.getCache().size();
            if(removed > 0) {
                log.debug("Удалено {} URL найденных в БД", removed);
            }
        }
        if(!urlCache.getCache().isEmpty()) urlCache.clear();
    }
}
