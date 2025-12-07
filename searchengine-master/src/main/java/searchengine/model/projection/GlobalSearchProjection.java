package searchengine.model.projection;

import searchengine.model.entity.Page;

public interface GlobalSearchProjection {
    Page getPage();
    Double getTotalRank();
}
