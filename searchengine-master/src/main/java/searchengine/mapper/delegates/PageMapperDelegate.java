package searchengine.mapper.delegates;

import org.springframework.stereotype.Component;
import searchengine.core.dto.PageDto;
import searchengine.mapper.PageMapper;
import searchengine.model.entity.Page;

@Component
public class PageMapperDelegate implements PageMapper {

    @Override
    public Page dtoToEntity(PageDto dto) {
        Page page = new Page();
        page.setSite(dto.getSite());
        page.setPath(dto.getPath());
        page.setCode(dto.getCode());
        page.setContent(dto.getContent());
        return page;
    }
}
