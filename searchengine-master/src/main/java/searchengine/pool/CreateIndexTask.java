package searchengine.pool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.core.config.AppContext;
import searchengine.core.dto.SearchIndexDto;

import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
@Slf4j
public class CreateIndexTask extends RecursiveAction {
    private final AppContext context;
    private final SearchIndexDto indexDto;
    @Override
    protected void compute() {
        if (!context.indexingRunning.get()) return;
        context.dataProcessor.saveIndexFromDto(indexDto);
    }
}
