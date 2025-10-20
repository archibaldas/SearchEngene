package searchengine.pool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;

@Configuration
@Slf4j
public class PoolConfiguration {
    @Bean(destroyMethod = "shutdown")
    public ForkJoinPool createPool(){
        return new ForkJoinPool(Math.min(32, Runtime.getRuntime().availableProcessors() * 2),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (t, e) -> log.error("Ошибка: {} в потоке {}", e.getMessage(), t.getName()),
                true);
    }
}
