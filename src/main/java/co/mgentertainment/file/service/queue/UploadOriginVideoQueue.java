package co.mgentertainment.file.service.queue;

import co.mgentertainment.common.utils.queue.DisruptorQueue;
import co.mgentertainment.file.service.config.MgfsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description UploadOriginVideoQueue
 */
@Component
@DependsOn("uploadOriginVideoConsumer")
@RequiredArgsConstructor
public class UploadOriginVideoQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {
    private final ObjectProvider<UploadOriginVideoConsumer> objectProvider;
    private final MgfsProperties mgfsProperties;
    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() throws Exception {
        int workerSize = Optional.ofNullable(mgfsProperties.getUploadOriginThreadPoolSize()).orElse(Runtime.getRuntime().availableProcessors());
        UploadOriginVideoConsumer[] consumers = new UploadOriginVideoConsumer[workerSize];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = objectProvider.getIfAvailable();
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(workerSize, workerSize * 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("upload-origin-worker-" + RandomStringUtils.randomAlphanumeric(4));
            return thread;
        });
        // buffer size:131072
        this.queue = (DisruptorQueue<T>) new DisruptorQueue<>(2 << 17, false, executor, consumers);
    }

    @Override
    public void put(T t) {
        this.queue.add(t);
    }

    @Override
    public void close() {
        this.queue.shutdown();
    }

    @Override
    public void destroy() throws Exception {
        close();
    }
}
