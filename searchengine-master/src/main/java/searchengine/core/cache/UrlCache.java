package searchengine.core.cache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class UrlCache {
    private final Set<String> cache = ConcurrentHashMap.newKeySet();

    public boolean shouldProcess(String url){
        return !cache.contains(url);
    }

    public void add(String url){
        cache.add(url);
    }

    public void removeIfExists(String url){
        cache.remove(url);
    }
}
