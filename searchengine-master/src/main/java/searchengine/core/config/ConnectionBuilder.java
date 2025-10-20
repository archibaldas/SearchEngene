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
                .followRedirects(true);
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
