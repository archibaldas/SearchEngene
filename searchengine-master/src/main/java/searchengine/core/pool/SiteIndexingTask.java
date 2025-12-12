package searchengine.core.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.core.cache.UrlCache;
import searchengine.core.components.PageContentExtractor;
import searchengine.model.entity.SiteEntity;
import searchengine.model.services.SiteService;
import searchengine.web.services.IndexingPageService;

import java.util.concurrent.CompletionException;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static searchengine.core.utils.SiteStatusUtils.*;

@RequiredArgsConstructor
@Slf4j
public class SiteIndexingTask extends RecursiveAction {
    private final AtomicBoolean indexingRunning;
    private final IndexingPageService indexingPageService;
    private final PageContentExtractor pageContentExtractor;
    private final SiteService siteService;
    private final UrlCache urlCache;
    private final Site site;

    @Override
    protected void compute() {
        SiteEntity siteEntity = siteService.create(site);
        log.info("Сайт с адресом: {} сохранен в базе данных с индексом: {}", site.getUrl(), siteEntity.getId());

        PageParserTask parserTask = new PageParserTask(indexingRunning,
                indexingPageService,
                pageContentExtractor,
                siteService,
                urlCache,
                siteEntity,
                siteEntity.getUrl());
        try {
            log.info("Запуск индексации сайта: {}", site.getUrl());

            if(!indexingRunning.get()){
                siteService.updateStatus(setFailedStatus(siteEntity, STOP_INDEXING));
            }
            siteService.updateStatus(setIndexingProcessStatus(siteEntity));

            parserTask.fork();
            parserTask.join();

            log.info("Индексация сайта: {} завершена.", site.getUrl());
            siteService.updateStatus(setIndexedStatus(siteEntity));

        } catch (CompletionException e) {
            Throwable originalCause = e.getCause();
            log.warn("Индексация сайта: {} завершена с ошибкой. {}", site.getUrl(), originalCause.getMessage());
        } catch (Exception e) {
            log.warn("Неожиданная ошибка при индексации сайта {}: {}", site, e.getMessage());
            siteService.updateStatus(setFailedStatus(siteEntity, e.getMessage()));
        } finally {
            if (!indexingRunning.get()) {
                siteService.updateStatus(setFailedStatus(siteEntity, STOP_INDEXING));
            }
        }
    }
}
