package searchengine.core.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.core.cache.UrlCache;
import searchengine.core.utils.HtmlUtils;
import searchengine.services.DataProcessorFacade;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinksExtractor {

    private final DataProcessorFacade dataProcessorFacade;
    private final UrlCache urlCache;
    private static final Set<String> FILE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif",
            "webp", "pdf", "eps","xls","xlsx", "doc", "pptx",
            "docx", "zip", "rar", "exe", "mp3", "mp4",
            "avi", "mkv", "tar", "gz", "js", "php", "dat", "nc", "fig", "m"
    );

    private static final List<String> INVALID_SUBSTRINGS = List.of(
            "#", "utm_", "ad/", "?", "%20"
    );

    public List<String> extract(String content, String baseUrl, String path) {
        Document document = HtmlUtils.stringToDocument(content, baseUrl);

        List<String> childLinks = document.select("a[href]")
                .stream()
                .map(e -> e.absUrl("href"))
                .filter(LinksExtractor::isValidLink)
                .filter(b -> !b.equals(baseUrl))
                .filter(l -> !isFile(l))
                .filter(a -> !isAuth(a))
                .filter(e -> isChildLink(e, baseUrl))
                .filter(p -> !isVisitedLink(p))
                .toList();
        if (!childLinks.isEmpty()) {
            log.debug("Извлечено {} дочерних ссылок к сайту [{}]", childLinks.size(),baseUrl + path);
        }
        return childLinks;
    }

    private static boolean isValidLink(String link) {
        try {
            URL url = new URL(link);
            if(!url.getProtocol().matches("https?")) return false;
            return INVALID_SUBSTRINGS.stream().noneMatch(link::contains);
        } catch (MalformedURLException e) {
            return false;
        }
    }


    private static boolean isChildLink(String link, String baseUrl) {
        return link.startsWith(baseUrl) && link.length() > baseUrl.length();
    }

    public static boolean isFile(String link) {
        String lower = link.toLowerCase();
        return FILE_EXTENSIONS.stream().anyMatch(ext -> lower.endsWith("." + ext));
    }

    public boolean isVisitedLink(String url){
        return dataProcessorFacade.existsPageLinkInDatabase(url) || !urlCache.shouldProcess(url);
    }

    private static boolean isAuth(String link){
        return link.toLowerCase().endsWith("auth");
    }
}
