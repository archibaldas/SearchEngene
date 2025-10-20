package searchengine.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.core.config.AppContext;
import searchengine.core.dto.PageDto;
import searchengine.core.dto.SearchIndexDto;
import searchengine.core.utils.HtmlUtils;
import searchengine.model.entity.Page;
import searchengine.model.entity.SiteEntity;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
@Slf4j
public class PageParserTask extends RecursiveAction {
    private final AppContext context;
    private final SiteEntity siteEntity;
    private final String url;

    @Override
    protected void compute() {
        if (!context.indexingRunning.get()) return;
        try {
            try {
                HtmlUtils.timeout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            PageDto pageDto = context.pageParser.parse(url, siteEntity);
            Page page = context.dataProcessor.saveOrUpdatePageToDatabase(pageDto);
            List<SearchIndexDto> indexDtos = context.pageContentExtractor.getIndexesFromPage(page);

            List<PageParserTask> parserTasks = pageDto.getChildLink().stream()
                    .map(link -> new PageParserTask(context, siteEntity, link))
                    .toList();
            if (parserTasks.isEmpty()) return;
            List<CreateIndexTask> indexTasks = indexDtos.stream()
                    .map(searchIndexDto -> new CreateIndexTask(context, searchIndexDto))
                    .toList();
            invokeAll(parserTasks);
            if (!indexTasks.isEmpty()){
                invokeAll(indexTasks);
            }
        } catch (RuntimeException e) {
            log.warn("Ошибка возникает в классе: {}", this.getClass().getName());
            throw new CompletionException(e);
        }
    }
}
