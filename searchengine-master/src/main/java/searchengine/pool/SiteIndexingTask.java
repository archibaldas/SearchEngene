package searchengine.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.core.config.AppContext;
import searchengine.exceptions.NoFoundEntityException;
import searchengine.model.entity.SiteEntity;

import java.util.concurrent.CompletionException;
import java.util.concurrent.RecursiveTask;

@RequiredArgsConstructor
@Slf4j
public class SiteIndexingTask extends RecursiveTask<SiteEntity> {

    private final Site site;
    private final AppContext context;

    @Override
    protected SiteEntity compute() {
        try{
            log.debug("Удаление данных сайта: {}", site.getUrl());
            context.dataProcessor.deleteAllBySite(site);
        } catch (NoFoundEntityException e){
            log.debug(e.getMessage());
        }
        SiteEntity siteEntity = context.dataProcessor.findOrCreateSiteInDatabase(site);
        log.info("Сайт с адресом: {} сохранен в базе данных с индексом: {}", site.getUrl(), siteEntity.getId());
        PageParserTask parserTask = new PageParserTask(context, siteEntity, siteEntity.getUrl());
        try {
            log.info("Запуск индексации сайта: {}", site.getUrl());
            if(!context.indexingRunning.get()){
                return context.dataProcessor.saveStatus(siteEntity, "Индексация остановлена пользователем");
            }
            context.dataProcessor.processedStatus(siteEntity);
            parserTask.fork();
            parserTask.join();
            log.info("Индексация сайта: {} завершена.", site.getUrl());
            return context.dataProcessor.saveStatus(siteEntity);
        } catch (CompletionException e) {
            Throwable originalCause = e.getCause();
            log.warn("Индексация сайта: {} завершена с ошибкой. {}", site.getUrl(), originalCause.getMessage());
            return context.dataProcessor.saveStatus(siteEntity, originalCause.getMessage());
        }
    }
}
