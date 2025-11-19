package searchengine.core.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.UnsupportedMimeTypeException;
import org.springframework.stereotype.Service;
import searchengine.core.config.ConnectionBuilder;
import searchengine.core.config.ParseConfiguration;
import searchengine.core.dto.PageDto;
import searchengine.exceptions.PageIndexingException;
import searchengine.exceptions.ParsingException;
import searchengine.model.entity.SiteEntity;
import searchengine.model.repositories.PageRepository;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import static searchengine.core.utils.HtmlUtils.*;
import static searchengine.core.utils.HttpStatusCodeRusMessenger.getMessageByCode;

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
    private final int[] statusCodes = new int[]{403, 500};

    private final PageRepository pageRepository;

    private int attempt = 0;

    public PageDto parse(String url, SiteEntity site) throws ParsingException, PageIndexingException {
        PageDto pageDto = parseWithAttempts(url, site, 0, false);
        if(normalizeUrl(url).equals(normalizeUrl(site.getUrl())) && pageDto.getCode() >= 400) {
            log.warn("Ошибка стартовой страницы: {}", getMessageByCode(pageDto.getCode()));
            throw new PageIndexingException("индексации", "Главная страница недоступна.",getMessageByCode(pageDto.getCode()));
        }
        return pageDto;
    }

    private PageDto parseWithAttempts(String url, SiteEntity site, int attempt, boolean useChromeAgent)
            throws ParsingException, PageIndexingException{
        try {
            timeout();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ParsingException("таймаута", url, e);
        }

        log.debug("Установка соединения со страницей по адресу: {}", url);
        PageDto pageDto = createPageDto(url, site);
        Connection.Response response;
        url = pageDto.getSite().getUrl() + pageDto.getPath();

        try {
            response = getResponse(url, useChromeAgent);
        } catch (ParsingException e){

            pageDto.setCode(404);
            return createErrorPageDto(pageDto);
        }

        if (response == null) {
            pageDto.setCode(400);
            return createErrorPageDto(pageDto);
        }



        int statusCode = response.statusCode();

        pageDto.setCode(statusCode);

        if (isStatusCodeToNextAttempt(statusCode) && attempt < 3) {
            boolean nextUseChromeAgent = useChromeAgent || statusCode == 403;

            log.warn("Получен {}, для Url: {}. Повторная попытка: {}. UseChromeAgent: {}",
                    statusCode, url, attempt + 1, nextUseChromeAgent);

            if (statusCode == 500) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return parseWithAttempts(url, site, attempt + 1, nextUseChromeAgent);
        }

        if (statusCode >= 400) {
            log.debug("Статус страницы:{} - {} : {}",
                    pageDto.getSite().getUrl() + pageDto.getPath(),
                    statusCode, response.statusMessage());
            return createErrorPageDto(pageDto);
        }

        return parseSuccessfulResponse(response, pageDto);
    }

    private boolean isStatusCodeToNextAttempt(int code){
        for (int statusCode : statusCodes ){
            if(code == statusCode){
                return true;
            }
        }
        return false;
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

    private Connection.Response getResponse(String url, boolean useChromeAgent) throws ParsingException {
        try {
            if(useChromeAgent){
                return connectionBuilder
                        .withChromeAgent()
                        .build(url)
                        .execute();
            }

            return connectionBuilder
                    .withCustomUserAgent(parseConfiguration.getUserAgent())
                    .build(url)
                    .execute();
        } catch (UnsupportedMimeTypeException ex){
            log.warn("Ссылка: [{}] возрващает неверный тип данных: {}", url, ex.getMessage());
            return null;
        } catch (UnknownHostException uex){
            log.warn("Ошибка обработки ссылки: [{}] Неизвестный хост: {}", url, uex.getMessage());
            return null;
        } catch (IOException e) {
            log.warn("Ошибка подключения к {}: {}", url, e.getMessage());
            throw new ParsingException("подключения", url, e);
        }
    }

    private PageDto createErrorPageDto(PageDto pageDto){
        pageDto.setContent("");
        return pageDto;
    }

    private PageDto parseSuccessfulResponse(Connection.Response response, PageDto pageDto) throws ParsingException{
        try{
            String content = documentToString(response.parse());
            pageDto.setContent(content);
            return pageDto;
        } catch (IOException e) {
            throw new ParsingException( "парсинг контeнта", pageDto.getSite().getUrl() + pageDto.getPath(), e);
        }
    }
}
