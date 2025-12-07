package searchengine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@Slf4j
public class PoolConfiguration {
    @Bean
    @Scope("singleton")
    public ForkJoinPool createPool(){
        return new ForkJoinPool(Math.min(32, Runtime.getRuntime().availableProcessors() * 2),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (t, e) -> log.error("Ошибка: {} в потоке {}", e.getMessage(), t.getName()),
                true);
    }

    @Bean
    public AtomicBoolean indexingRunning(){
        return new AtomicBoolean(false);
    }
}
