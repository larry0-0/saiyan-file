package co.saiyan.file.service.queue;

import co.saiyan.common.utils.queue.DisruptorQueue;
import co.saiyan.file.service.config.MgfsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description PrintWatermarkQueue
 */
@Component
@DependsOn("printWatermarkConsumer")
@RequiredArgsConstructor
public class PrintWatermarkQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {
    private final ObjectProvider<PrintWatermarkConsumer> objectProvider;
    private final MgfsProperties mgfsProperties;
    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() {
        int workerSize = Optional.ofNullable(mgfsProperties.getPrintWatermarkThreadPoolSize()).orElse(Runtime.getRuntime().availableProcessors());
        PrintWatermarkConsumer[] consumers = new PrintWatermarkConsumer[workerSize];
        for (int i = 0; i < workerSize; i++) {
            consumers[i] = objectProvider.getIfAvailable();
        }
        ExecutorService executor = Executors.newFixedThreadPool(workerSize, r -> {
            Thread thread = new Thread(r);
            thread.setName("print-watermark-worker-" + RandomStringUtils.randomAlphanumeric(4));
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
