package co.mgentertainment.file.service.queue;

import co.mgentertainment.common.utils.queue.DisruptorQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description UploadTrailerQueue
 */
@Component
@RequiredArgsConstructor
public class UploadTrailerQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {
    private final ThreadPoolExecutor disruptorWorkPool;
    private final UploadTrailerConsumer uploadTrailerConsumer;

    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() throws Exception {
        // worker size = cpu core number
        UploadTrailerConsumer[] consumers = new UploadTrailerConsumer[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = uploadTrailerConsumer;
        }
        // buffer size:131072
        this.queue = DisruptorQueue.independentPubSubInstance(2 << 17, false, disruptorWorkPool, consumers);
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
