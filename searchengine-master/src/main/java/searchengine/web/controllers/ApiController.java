package searchengine.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.web.services.IndexingService;
import searchengine.web.services.SearchService;
import searchengine.web.services.dto.responses.ResultResponse;
import searchengine.web.services.dto.responses.SearchResponse;
import searchengine.web.services.dto.statistics.StatisticsResponse;
import searchengine.web.services.StatisticsService;

@SuppressWarnings("ALL")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    @GetMapping("/startIndexing")
    public ResultResponse startIndexing(){
        indexingService.startIndexing();
        return new ResultResponse(true);
    }

    @GetMapping("/stopIndexing")
    public ResultResponse stopIndexing(){
        indexingService.stopIndexing();
        return new ResultResponse(true);
    }

    @PostMapping("/indexPage")
    public ResultResponse indexPage(
            @RequestParam String url){
        indexingService.indexPage(url);
        return new ResultResponse(true);
    }

    @GetMapping("/search")
    public SearchResponse search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "") String site,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "20") int limit
    )
    {
        return searchService.search(query, site, offset,limit);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
