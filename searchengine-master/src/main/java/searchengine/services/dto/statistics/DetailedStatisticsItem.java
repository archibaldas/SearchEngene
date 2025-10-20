package searchengine.services.dto.statistics;

import lombok.Data;

@Data
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private String status = "NOT INDEXED";
    private long statusTime;
    private String error;
    private int pages;
    private int lemmas;
}
