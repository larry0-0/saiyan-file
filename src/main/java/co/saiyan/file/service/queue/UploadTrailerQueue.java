package co.saiyan.file.service.queue;

import co.saiyan.common.utils.queue.DisruptorQueue;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description UploadTrailerQueue
 */
@Component
@DependsOn("uploadTrailerConsumer")
@RequiredArgsConstructor
public class UploadTrailerQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {
    private final ObjectProvider<UploadTrailerConsumer> objectProvider;
    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() throws Exception {
        int workerSize = NumberUtils.INTEGER_ONE;
        UploadTrailerConsumer[] consumers = new UploadTrailerConsumer[workerSize];
        for (int i = 0; i < workerSize; i++) {
            consumers[i] = objectProvider.getIfAvailable();
        }
        ExecutorService executor = Executors.newFixedThreadPool(workerSize, r -> {
            Thread thread = new Thread(r);
            thread.setName("upload-trailer-worker-" + RandomStringUtils.randomAlphanumeric(4));
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
