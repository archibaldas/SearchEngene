package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.core.utils.HtmlUtils;
import searchengine.services.IndexingService;
import searchengine.services.dto.responses.ErrorResponse;
import searchengine.services.dto.responses.ResultResponse;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.core.config.AppContext;
import searchengine.core.dto.PageDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;
import searchengine.pool.SiteIndexingTask;

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.*;

import static searchengine.core.utils.HtmlUtils.normalizeUrl;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final AppContext context;
    private final SitesList sitesList;
    private final ForkJoinPool forkJoinPool;
    private volatile Future<?> indexingFuture;

    @Override
    @Synchronized
    public Object startIndexing(){
        if(context.indexingRunning.get()){
            return new ErrorResponse(false, "Индексация уже запущена.");
        }
        context.indexingRunning.set(true);
        List<SiteIndexingTask> indexingTasks = sitesList.getSites().stream()
                .map(site -> new SiteIndexingTask(site, context))
                .toList();
        indexingFuture = forkJoinPool.submit(() -> {
            try{
                log.info("Индексация запущена в фоновом режиме");
                indexingTasks.forEach(forkJoinPool::execute);
                indexingTasks.forEach(ForkJoinTask::join);
            } catch (RuntimeException t){
                log.error("Общая ошибка во время индексации {}", t.getMessage());
            } finally {
                context.indexingRunning.set(false);
                log.info("Индексация завершена");
            }
        });
        return new ResultResponse(true);
    }

    @Override
    @Synchronized
    public Object stopIndexing(){
        if(!context.indexingRunning.get()){
            return new ErrorResponse(false, "Индексация остановлена.");
        }
        context.indexingRunning.set(false);
        if(indexingFuture != null && !indexingFuture.isDone()){
            boolean cancelled = indexingFuture.cancel(true);
            log.debug("Состояние отмены задачи индексации: {}", cancelled);
        }
        if(forkJoinPool != null){
            forkJoinPool.shutdownNow();
        }
        indexingFuture = null;
        return new ResultResponse(true);
    }

    @Override
    public Object indexPage(String url) {
        log.debug("Индексация страницы {}", url);
        String baseUrl;
        try {
            baseUrl = HtmlUtils.getBaseUrl(url);
        } catch (MalformedURLException e) {
            log.debug("Ошибочный формат ссылки: {}, Ошибка: {}", url, e.getMessage());
            return new ErrorResponse(false, MessageFormat
                    .format("Ошибочный формат ссылки: {0}, Ошибка: {1}", url, e.getMessage()));
        }
        if(!isUrlAllowed(baseUrl)){
            return new ErrorResponse(false, MessageFormat
                    .format("Индексируемая страница: {0} не относится к списку представленному списку индексации", url));
        }

        try {
            Site site = (Site) sitesList.getSites().stream()
                    .filter(s -> s.getUrl().equals(baseUrl));
            SiteEntity siteEntity = context.dataProcessor.findOrCreateSiteInDatabase(site);
            PageDto pageDto = context.pageParser.parse(url, siteEntity);
            Page page = context.dataProcessor.saveOrUpdatePageToDatabase(pageDto);
            List<SearchIndexDto> indexDtos = context.pageContentExtractor.getIndexesFromPage(page);
            indexDtos.forEach(context.dataProcessor::saveIndexFromDto);
        } catch (Exception e) {
            return new ErrorResponse(false, e.getMessage());
        }
        return new ResultResponse(true);
    }


    public boolean isUrlAllowed(String inputUrl){
        return sitesList.getSites().stream()
                .anyMatch(s -> normalizeUrl(inputUrl)
                        .equals(normalizeUrl(s.getUrl())));
    }
}
