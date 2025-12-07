package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import searchengine.exceptions.ParsingException;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Random;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "app.connection")
public class ParseConfiguration {
    private String userAgent;
    private List<String> chromeUserAgents;
    private String referer;
    private Integer timeout;

    public String getRandomChromeAgent(){
        if(chromeUserAgents == null || chromeUserAgents.isEmpty()){
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        }
        return chromeUserAgents.get(new Random().nextInt(chromeUserAgents.size()));
    }

    public String getEmptyReferer(){
        return "";
    }


    @Bean
    public SSLSocketFactory sslSocketFactory(){
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new ParsingException("создания SSLSocketFactory c отключенной проверкой SSL", "сайтов с собственным сертификатом", e.getCause());
        }
    }
}
