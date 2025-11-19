package searchengine.core.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocketFactory;

@Component
@RequiredArgsConstructor
@Getter
public class ConnectionBuilder {

    private final ParseConfiguration parseConfiguration;
    private final SSLSocketFactory sslSocketFactory;

    private String userAgent;
    private boolean useChromeAgent = false;

    public ConnectionBuilder withChromeAgent(){
        this.useChromeAgent = true;
        return this;
    }

    public ConnectionBuilder withCustomUserAgent(String userAgent){
        this.userAgent = userAgent;
        return this;
    }

    public Connection build(String url){
        Connection connection = Jsoup.connect(url)
                .userAgent(getEffectiveUserAgent())
                .timeout(parseConfiguration.getTimeout())
                .referrer(parseConfiguration.getReferer())
                .sslSocketFactory(sslSocketFactory)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .maxBodySize(0) // без ограничения размера
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                ;
        reset();
        return connection;
    }

    private String getEffectiveUserAgent(){
        return useChromeAgent ? parseConfiguration.getUserAgent() : userAgent;
    }

    private void reset() {
        this.userAgent = parseConfiguration.getUserAgent();
        this.useChromeAgent = false;
    }
}
