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
 * @description ConvertVideoQueue
 */
@Component
@RequiredArgsConstructor
public class ConvertVideoQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {
    private final ThreadPoolExecutor ffmpegWorkPool;
    private final ConvertVideoConsumer convertVideoConsumer;

    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() {
        // worker size = cpu core number
        ConvertVideoConsumer[] consumers = new ConvertVideoConsumer[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = convertVideoConsumer;
        }
        // buffer size:131072
        this.queue = DisruptorQueue.independentPubSubInstance(2 << 17, false, ffmpegWorkPool, consumers);
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
