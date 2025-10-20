package searchengine.core.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.core.components.LemmaFinder;
import searchengine.core.components.PageContentExtractor;
import searchengine.core.components.PageParser;
import searchengine.core.components.SearchingUtils;
import searchengine.core.utils.*;
import searchengine.services.DataProcessorFacade;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Component
@RequiredArgsConstructor
public class AppContext {
    public final PageParser pageParser;
    public final DataProcessorFacade dataProcessor;
    public final HtmlUtils htmlUtils;
    public final AtomicBoolean indexingRunning = new AtomicBoolean(false);
    public final PageContentExtractor pageContentExtractor;
    public final LemmaFinder lemmaFinder;
    public final MathUtils mathUtils;
    public final SearchingUtils searchingUtils;
}
