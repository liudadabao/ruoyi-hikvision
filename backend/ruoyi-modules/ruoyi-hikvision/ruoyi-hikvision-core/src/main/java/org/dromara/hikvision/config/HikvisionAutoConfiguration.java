package org.dromara.hikvision.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 海康 SDK 模块自动配置。
 *
 * @author hikvision-sdk
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(HikvisionProperties.class)
public class HikvisionAutoConfiguration {

    /**
     * SDK 专用线程池，用于回调后的异步业务处理（避免阻塞 SDK 回调线程）。
     */
    @Bean("hikvisionTaskExecutor")
    public Executor hikvisionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(512);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("hikvision-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
