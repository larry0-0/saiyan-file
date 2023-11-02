package co.mgentertainment.file.service.config;

import co.mgentertainment.common.devent.annonation.EnableDistributedEvent;
import co.mgentertainment.common.doc.annonation.EnableCommonDoc;
import co.mgentertainment.common.eventbus.annonation.EnableCommonEventBus;
import co.mgentertainment.common.syslog.annotation.EnableCommonSyslog;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CommonConfig
 */
@Configuration
@EnableCommonEventBus
@EnableCommonDoc
@EnableCommonSyslog
@EnableDistributedEvent
@EnableConfigurationProperties(MgfsProperties.class)
public class CommonConfig {

    @Bean(name = "fileUploadThreadPool")
    ThreadPoolExecutor fileUploadThreadPool(final MgfsProperties mgfsProperties) {
        Integer maxUploadPoolSize = Optional.ofNullable(mgfsProperties.getMaxUploadPoolSize()).orElse(50);
        return new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxUploadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("upload-executor-" + RandomStringUtils.randomAlphanumeric(4));
            return thread;
        });
    }

    @Bean(name = "ffmpegWorkPool")
    ThreadPoolExecutor ffmpegWorkPool() {
        int coreSize = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(coreSize, coreSize * coreSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("ffmpeg-worker-" + RandomStringUtils.randomAlphanumeric(4));
            return thread;
        });
    }
}