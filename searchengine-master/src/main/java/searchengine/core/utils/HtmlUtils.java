package searchengine.core.utils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@UtilityClass
@RequiredArgsConstructor
@Slf4j
public class HtmlUtils {

    public static void timeout() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500 + ThreadLocalRandom.current().nextInt(500));

    }

    public static String getTitleFromHtml(String content){
        return Jsoup.parse(content).title();
    }

    public static String getBaseUrl(String url) throws MalformedURLException{
        URL parsedUrl = new URL(url);
        int port = parsedUrl.getPort();
        boolean hasPort = port != -1 && port != 80 && port != 443;
        return parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + ((hasPort ? ":" + port : ""));
    }

    public static String getPath(String url) throws MalformedURLException{
        URL parsedUrl = new URL(url);
        String path = parsedUrl.getPath();
        if(path.endsWith("/") && path.length() > 1){
            path = normalizeUrl(path);
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    public static String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static String documentToString(Document doc){
        return doc.html();
    }

    public static Document stringToDocument(String html, String baseUrl){
        return Jsoup.parse(html,baseUrl);
    }

    public static String getCleanTextFromContent(String content){
        return Jsoup.parse(content).text();
    }
}
