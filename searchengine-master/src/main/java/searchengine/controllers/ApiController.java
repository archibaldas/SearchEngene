package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.dto.responses.ResultResponse;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;

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
    public ResponseEntity<Object> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "") String site,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "20") int limit
    )
    {
        return ResponseEntity.ok(searchService.search(query, site, offset,limit));
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
