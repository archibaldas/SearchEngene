package searchengine.web.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.web.services.StatisticsService;
import searchengine.web.services.dto.statistics.DetailedStatisticsItem;
import searchengine.web.services.dto.statistics.StatisticsData;
import searchengine.web.services.dto.statistics.StatisticsResponse;
import searchengine.web.services.dto.statistics.TotalStatistics;
import searchengine.model.services.LemmaService;
import searchengine.model.services.PageService;
import searchengine.model.services.SiteService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final AtomicBoolean indexingRunning;

    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setPages(pageService.count());
        total.setLemmas(lemmaService.count());
        total.setIndexing(indexingRunning.get());

        List<DetailedStatisticsItem> detailed = siteService.getDetailedStatisticItems(sites);

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
