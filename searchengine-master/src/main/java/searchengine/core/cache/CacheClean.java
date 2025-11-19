package searchengine.core.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import searchengine.services.DataProcessorFacade;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheClean {

    private final UrlCache urlCache;
    private final DataProcessorFacade dataProcessor;

    @Scheduled(fixedRate = 10000)
    public void removeFoundedUrls(){
        int before = urlCache.getCache().size();

        Set<String> urlsToCheck = new HashSet<>(urlCache.getCache());

        for(String url : urlsToCheck){
            if(dataProcessor.existsPageLinkInDatabase(url)){
                urlCache.removeIfExists(url);
            }
        }

        int removed = before - urlCache.getCache().size();
        if(removed > 0) {
            log.info("Удалено {} URL найденных в БД", removed);
        }
    }
}
