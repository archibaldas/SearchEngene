package searchengine.core.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.UnsupportedMimeTypeException;
import org.springframework.stereotype.Service;
import searchengine.config.ParseConfiguration;
import searchengine.core.dto.PageDto;
import searchengine.exceptions.PageIndexingException;
import searchengine.exceptions.ParsingException;
import searchengine.exceptions.SiteIndexingException;
import searchengine.model.entity.SiteEntity;
import searchengine.model.repositories.PageRepository;
import searchengine.web.services.StatisticsService;
import searchengine.web.services.impl.StatisticsServiceImpl;

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
    private final SSLSocketFactory sslSocketFactory;
    private final int[] statusCodes = new int[]{403, 500};

    private final PageRepository pageRepository;

    private final StatisticsService statisticsService;

    private final StatisticsServiceImpl statisticsServiceImpl;

    private int attempt = 0;

    public PageDto parse(String url, SiteEntity site) throws ParsingException, PageIndexingException {
        PageDto pageDto = parseWithAttempts(url, site, 0, false);
        if(normalizeUrl(url).equals(normalizeUrl(site.getUrl())) && pageDto.getCode() >= 400) {
            log.debug("Ошибка стартовой страницы: {}", getMessageByCode(pageDto.getCode()));
            throw new SiteIndexingException("Главная страница сайта: ", site.getUrl(), " недоступна. ",getMessageByCode(pageDto.getCode()));
        }
        return pageDto;
    }

    private PageDto parseWithAttempts(String url, SiteEntity site, int attempt, boolean useChromeAgent)
            throws ParsingException {

        log.debug("Установка соединения со страницей по адресу: {}", url);
        PageDto pageDto = createPageDto(url, site);
        Connection.Response response;
        url = pageDto.getSite().getUrl() + pageDto.getPath();

        try {
            response = getResponse(url, useChromeAgent);
        } catch (ParsingException e){
            pageDto.setCode(500);
            return createErrorPageDto(pageDto);
        }
        int statusCode = response.statusCode();

        pageDto.setCode(statusCode);

        if (isStatusCodeToNextAttempt(statusCode) && attempt < 3) {
            boolean nextUseChromeAgent = useChromeAgent ||
//                    Arrays.stream(statusCodes).anyMatch(sc -> sc == statusCode)
                    statusCode >= 400
                    ;

            log.debug("{} для Url: {}. Повторная попытка: {}. UseChromeAgent: {}",
                    getMessageByCode(statusCode), url, attempt + 1, nextUseChromeAgent);

            if (statusCode >= 500) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
//                nextUseChromeAgent = true;
            }

            return parseWithAttempts(url, site, attempt + 1, nextUseChromeAgent);
        }

        if (statusCode >= 400) {
            log.debug("Страница [{}] сохранена со статусом: {} : {}",
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
            throw new ParsingException("Ошибка валидации ссылки: [", url, "] Ошибка: ", e.getMessage());
        }
        return pageDto;
    }

    private Connection.Response getResponse(String url, boolean useChromeAgent) throws ParsingException {
        try {
            Connection.Response response;
            if(useChromeAgent){
                response = connectionBuilder
                        .withChromeAgent()
                        .build(url)
                        .execute();
            } else {
                response = connectionBuilder
                        .withCustomUserAgent(parseConfiguration.getUserAgent())
                        .build(url)
                        .execute();
            }

            if(response.statusCode() >= 400){
                log.debug("Ошибка {} для Url: {}", response.statusCode(), url);
                log.debug("Заголовок ответа: {}", response.headers());
                log.debug("Status message: {}", response.statusMessage());
            }

            return response;

        } catch (UnsupportedMimeTypeException ex){
            log.warn("Ссылка: [{}] возвращает неверный тип данных: {}", url, ex.getMessage());
            throw new ParsingException("Ссылка: [", url, "] возвращает неверный тип данных. Ошибка: ", ex.getMessage());
        } catch (UnknownHostException uex){
            log.warn("Ошибка обработки ссылки: [{}] Неизвестный хост: {}", url, uex.getMessage());
            throw new ParsingException("Неизвестный хост ссылки: [", url, "] Ошибка: ", uex.getMessage());
        } catch (IOException e) {
            log.debug("Ошибка подключения к {}: {}", url, e.getMessage());
            throw new ParsingException("Ошибка подключения к ссылке: [", url, "] Ошибка: ", e.getMessage());
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
            throw new ParsingException( "Ошибка парсинга контента страницы: [", pageDto.getSite().getUrl() + pageDto.getPath(),"] Ошибка: ", e);
        }
    }
}
