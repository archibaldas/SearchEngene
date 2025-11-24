package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.exceptions.IndexingProcessException;
import searchengine.services.IndexingService;
import searchengine.config.SitesList;
import searchengine.core.config.AppContext;
import searchengine.model.entity.SiteEntity;
import searchengine.pool.SiteIndexingTask;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;

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
    public void startIndexing(){
        Instant start = Instant.now();
        StringBuilder logSitesList = new StringBuilder();
        String logSites;
        for (Site site : sitesList.getSites()){
            logSitesList.append(site.getUrl()).append(", ");
        }
        logSites = logSitesList.toString();
        log.info("Запуск индексации сайтов: {}", logSites);
        if(context.indexingRunning.get()){
            throw new IndexingProcessException("уже");
        }
        context.indexingRunning.set(true);
        context.dataProcessor.deleteNoListSitesFromDb(sitesList);

        List<SiteIndexingTask> indexingTasks = sitesList.getSites().stream()
                .map(site -> new SiteIndexingTask(site, context))
                .toList();

        indexingFuture = forkJoinPool.submit(() -> {
            log.info("Индексация запущена в фоновом режиме");
            try{
                indexingTasks.forEach(forkJoinPool::execute);
                indexingTasks.forEach(ForkJoinTask::join);
            } catch (RuntimeException t){
                log.error("Общая ошибка во время индексации {}", t.getMessage());
            } finally {
                context.indexingRunning.set(false);
                context.cacheClean.removeFoundedUrls();
                log.info("Индексация завершена");
                log.info("Время индексации: [{} мин.]", Duration.between(start, Instant.now()).toMinutes());
            }
        });
    }

    @Override
    @Synchronized
    public void stopIndexing(){
        if(!context.indexingRunning.get()){
            throw new IndexingProcessException("не");
        }

        log.info("Остановка индексации пользователем.");
        context.indexingRunning.set(false);

        sitesList.getSites().forEach(site -> {
            try {
                SiteEntity siteEntity = context.dataProcessor.findSite(site);
                context.dataProcessor.saveStatus(siteEntity, "Индексация остановлена пользователем");
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
    public void indexPage(String url) {
        log.debug("Индексация страницы {}", url);
            context.dataProcessor.deletePageWithDataByUrl(url);
            context.indexingPageService.indexing(url);
    }
}
