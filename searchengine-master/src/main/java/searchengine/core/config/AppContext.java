package searchengine.core.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.core.cache.CacheClean;
import searchengine.core.cache.UrlCache;
import searchengine.core.components.*;
import searchengine.services.DataProcessorFacade;
import searchengine.services.IndexingPageService;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Component
@Scope("singleton")
@RequiredArgsConstructor
public class AppContext {
    public final PageParser pageParser;
    public final DataProcessorFacade dataProcessor;
    public final AtomicBoolean indexingRunning = new AtomicBoolean(false);
    public final PageContentExtractor pageContentExtractor;
    public final LemmaFinder lemmaFinder;
    public final SearchingUtils searchingUtils;
    public final UrlCache urlCache;
    public final IndexingPageService indexingPageService;
    public final CacheClean cacheClean;
}
