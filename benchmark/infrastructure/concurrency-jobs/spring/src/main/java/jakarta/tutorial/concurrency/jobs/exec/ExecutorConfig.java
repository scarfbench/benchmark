package jakarta.tutorial.concurrency.jobs.exec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ExecutorConfig {

    @High
    @Bean
    public ThreadPoolExecutor highExecutor() {
        int cores = Math.max(4, Runtime.getRuntime().availableProcessors());
        return new ThreadPoolExecutor(
                cores, cores * 2, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10_000),
                new ThreadFactoryBuilder("high"));
    }

    @Low
    @Bean
    public ThreadPoolExecutor lowExecutor() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        return new ThreadPoolExecutor(
                cores, cores, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2_000),
                new ThreadFactoryBuilder("low"));
    }

    static class ThreadFactoryBuilder implements ThreadFactory {
        private final String pool;
        private final ThreadFactory delegate = Executors.defaultThreadFactory();
        ThreadFactoryBuilder(String pool) { this.pool = pool; }
        @Override public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setName("jobs-" + pool + "-" + t.getId());
            t.setDaemon(false);
            return t;
        }
    }
}