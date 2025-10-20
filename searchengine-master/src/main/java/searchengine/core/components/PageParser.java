package searchengine.core.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import searchengine.core.config.ConnectionBuilder;
import searchengine.core.config.ParseConfiguration;
import searchengine.core.dto.PageDto;
import searchengine.core.utils.HtmlUtils;
import searchengine.exceptions.ParsingException;
import searchengine.model.entity.SiteEntity;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static searchengine.core.utils.HtmlUtils.*;

@Service
@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class PageParser {

    private final ConnectionBuilder connectionBuilder;
    private final ParseConfiguration parseConfiguration;
    private final LinksExtractor linksExtractor;
    private final SSLSocketFactory sslSocketFactory;
    private int attempt = 0;

    public PageDto parse(String url, SiteEntity site) throws ParsingException {
        try {
            timeout();
        } catch (InterruptedException e) {
            throw new ParsingException("таймаута", url, e.getCause());
        }
        log.debug("Установка соединения cо страницей по адресу: {}", url);
        PageDto pageDto = createPageDto(HtmlUtils.normalizeUrl(url), site);
        Connection.Response response = getResponse(url);
        if (response.statusCode() == 403 && attempt <= 3) {
            attempt++;
            log.warn("Получен {}, для Url: {} переключаемся на useChromeAgent. Попытка: {}", response.statusCode(), url, attempt);
            parse(url, site);
        }
        return processedResponse(response, pageDto);
    }

    private PageDto createPageDto(String url, SiteEntity site) throws ParsingException{
        PageDto pageDto = new PageDto();
        pageDto.setSite(site);
        try{
            pageDto.setPath(getPath(url));
        } catch (MalformedURLException e) {
            throw new ParsingException( "валидации URL", url,  e);
        }
        return pageDto;
    }

    private Connection.Response getResponse(String url) throws ParsingException {
        try {
            if(attempt > 0){
                return connectionBuilder
                        .withChromeAgent()
                        .build(url)
                        .execute();
            }

            return connectionBuilder
                    .withCustomUserAgent(parseConfiguration.getUserAgent())
                    .build(url)
                    .execute();
        } catch (IOException e) {
            log.warn("Ошибка подключения к {}: {}", url, e.getMessage());
            throw new ParsingException("подключения", url, e);
        }
    }

    private PageDto createErrorPageDto(PageDto pageDto){
        pageDto.setContent("");
        pageDto.setChildLink(new ArrayList<>());
        return pageDto;
    }

    private PageDto processedResponse(Connection.Response response, PageDto pageDto ) throws ParsingException{
        int statusCode = response.statusCode();
        pageDto.setCode(statusCode);
        if (statusCode >= 400){
            log.warn("Статус страницы:{} - {} : {}", pageDto.getSite().getUrl() + pageDto.getPath(), statusCode, response.statusMessage());
            return createErrorPageDto(pageDto);
        }
        return parseSuccessfulResponse(response, pageDto);
    }

    private PageDto parseSuccessfulResponse(Connection.Response response, PageDto pageDto) throws ParsingException{
        try{
            String content = documentToString(response.parse());
            pageDto.setContent(content);
            pageDto.setChildLink(linksExtractor.extract(content, pageDto.getSite().getUrl(), pageDto.getPath()));
            return pageDto;
        } catch (IOException e) {
            throw new ParsingException( "парсинг контeнта", pageDto.getSite().getUrl() + pageDto.getPath(), e);
        }
    }
}
