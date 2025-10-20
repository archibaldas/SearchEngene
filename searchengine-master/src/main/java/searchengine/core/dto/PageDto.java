package searchengine.core.dto;

import lombok.Data;
import searchengine.model.entity.SiteEntity;

import java.util.List;

@Data
public class PageDto {
    private SiteEntity site;
    private String path;
    private Integer code;
    private String content;
    private List<String> childLink;
}
