package searchengine.web.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.core.cache.CacheClean;
import searchengine.core.cache.UrlCache;
import searchengine.core.components.PageContentExtractor;
import searchengine.exceptions.IndexingProcessException;
import searchengine.exceptions.PageIndexingException;
import searchengine.exceptions.SiteIndexingException;
import searchengine.model.entity.StatusType;
import searchengine.model.services.CleanUpService;
import searchengine.model.services.SiteService;
import searchengine.web.services.IndexingPageService;
import searchengine.web.services.IndexingService;
import searchengine.config.SitesList;
import searchengine.model.entity.SiteEntity;
import searchengine.core.pool.SiteIndexingTask;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static searchengine.core.utils.SiteStatusUtils.STOP_INDEXING;
import static searchengine.core.utils.SiteStatusUtils.setFailedStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final AtomicBoolean indexingRunning;
    private final IndexingPageService indexingPageService;
    private final PageContentExtractor pageContentExtractor;
    private final SiteService siteService;
    private final UrlCache urlCache;
    private final CacheClean cacheClean;
    private final CleanUpService cleanUpService;
    private final SitesList sitesList;
    private final ForkJoinPool forkJoinPool;
    private volatile Future<?> indexingFuture;

    @Override
    @Synchronized
    public void startIndexing(){
        Instant start = Instant.now();
        StringBuilder logSitesList = new StringBuilder();
        String logSites;
        for (Site site : sitesList.getSites()){
            logSitesList.append(site.getUrl()).append(", ");
        }
        logSites = logSitesList.toString();
        log.info("Запуск индексации сайтов: {}", logSites);
        if(indexingRunning.get()){
            throw new IndexingProcessException("Индексация уже запущена");
        }
        indexingRunning.set(true);
        cleanUpService.cleanUpAllTablesInDatabase();
        cacheClean.setIndexingRunning(indexingRunning);


        List<SiteIndexingTask> indexingTasks = sitesList.getSites().stream()
                .map(site -> new SiteIndexingTask(indexingRunning, indexingPageService, pageContentExtractor,
                        siteService, urlCache, site))
                .toList();

        indexingFuture = forkJoinPool.submit(() -> {
            log.info("Индексация запущена в фоновом режиме");
            try{
                indexingTasks.forEach(forkJoinPool::execute);
                indexingTasks.forEach(ForkJoinTask::join);
            } catch (RuntimeException t){
                log.error("Общая ошибка во время индексации {}", t.getMessage());
            } finally {
                indexingRunning.set(false);
                cacheClean.setIndexingRunning(indexingRunning);
                log.info("Индексация завершена");
                log.info("Время индексации: [{} мин.]", Duration.between(start, Instant.now()).toMinutes());
            }
        });
    }

    @Override
    @Synchronized
    public void stopIndexing(){
        if(!indexingRunning.get()){
            throw new IndexingProcessException("Индексация не запущена");
        }

        log.info("Остановка индексации пользователем.");
        indexingRunning.set(false);

        sitesList.getSites().forEach(site -> {
            try {
                SiteEntity siteEntity = siteService.findSite(site);
                if(siteEntity.getStatus().equals(StatusType.INDEXING)){
                    siteService.updateStatus(setFailedStatus(siteEntity, STOP_INDEXING));
                    cacheClean.setIndexingRunning(indexingRunning);
                }
            } catch (Exception e) {
                log.warn("Ошибка при установке статуса FAILED: {}", e.getMessage());
            }
        });

        if(indexingFuture != null && !indexingFuture.isDone()){
            boolean cancelled = indexingFuture.cancel(true);
            log.debug("Состояние отмены задачи индексации: {}", cancelled);
        }
    }

    @Override
    public void indexPage(String url) throws PageIndexingException, SiteIndexingException {
        log.debug("Индексация страницы {}", url);
            indexingPageService.deletePageWithDataByUrl(url);
            indexingPageService.indexing(url);
    }
}
