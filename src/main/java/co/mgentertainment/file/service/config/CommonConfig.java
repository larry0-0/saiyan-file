package co.mgentertainment.file.service.config;

import co.mgentertainment.common.devent.annonation.EnableDistributedEvent;
import co.mgentertainment.common.doc.annonation.EnableCommonDoc;
import co.mgentertainment.common.eventbus.annonation.EnableCommonEventBus;
import co.mgentertainment.common.syslog.annotation.EnableCommonSyslog;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

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
@Order(-100)
public class CommonConfig {

    @Bean(name = "file2s3ThreadPool")
    ThreadPoolExecutor fileUploadThreadPool() {
        int workerSize = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(workerSize, workerSize * 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("file2s3-executor-" + RandomStringUtils.randomAlphanumeric(4));
            return thread;
        });
    }
}