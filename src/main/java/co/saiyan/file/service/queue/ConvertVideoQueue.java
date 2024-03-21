package co.saiyan.file.service.queue;

import co.saiyan.common.utils.queue.DisruptorQueue;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description ConvertVideoQueue
 */
@Component
@DependsOn("convertVideoConsumer")
@RequiredArgsConstructor
public class ConvertVideoQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {
    private final ObjectProvider<ConvertVideoConsumer> objectProvider;
    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() {
        // worker size = cpu core number
        int workerSize = Runtime.getRuntime().availableProcessors();
        ConvertVideoConsumer[] consumers = new ConvertVideoConsumer[workerSize];
        for (int i = 0; i < workerSize; i++) {
            consumers[i] = objectProvider.getIfAvailable();
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(workerSize, workerSize * 4 + 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("convert-video-worker-" + RandomStringUtils.randomAlphanumeric(4));
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
