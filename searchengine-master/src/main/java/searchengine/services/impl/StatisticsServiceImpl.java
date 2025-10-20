package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.core.config.AppContext;
import searchengine.services.StatisticsService;
import searchengine.services.dto.statistics.DetailedStatisticsItem;
import searchengine.services.dto.statistics.StatisticsData;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.dto.statistics.TotalStatistics;
import searchengine.model.entity.SiteEntity;
import searchengine.model.services.LemmaService;
import searchengine.model.services.PageService;
import searchengine.model.services.SiteService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;

    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final AppContext context;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setPages(pageService.count());
        total.setLemmas(lemmaService.count());
        total.setIndexing(context.indexingRunning.get());

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteEntity> sitesList = siteService.findAll();
        for(SiteEntity siteEntity : sitesList){
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(siteEntity.getName());
            item.setUrl(siteEntity.getUrl());
            item.setPages(siteEntity.getPages().size());
            item.setLemmas(siteEntity.getLemmas().size());
            item.setStatus(siteEntity.getStatus().name());
            item.setError(siteEntity.getLastError());
            item.setStatusTime(siteEntity.getStatusTime().toEpochMilli());
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
