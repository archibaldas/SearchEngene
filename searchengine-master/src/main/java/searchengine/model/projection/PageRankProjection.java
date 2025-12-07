package searchengine.model.projection;

import searchengine.model.entity.Page;

public interface PageRankProjection {
    Page getPage();
    Double getTotalRank();
}
