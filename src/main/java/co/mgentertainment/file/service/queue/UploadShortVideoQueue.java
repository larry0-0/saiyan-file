package co.mgentertainment.file.service.queue;

import co.mgentertainment.common.utils.queue.DisruptorQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description UploadShortVideoQueue
 */
@Component
@RequiredArgsConstructor
public class UploadShortVideoQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {

    private final UploadShortVideoConsumer uploadShortVideoConsumer;
    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() throws Exception {
        // worker size = cpu core number
        UploadShortVideoConsumer[] consumers = new UploadShortVideoConsumer[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = uploadShortVideoConsumer;
        }
        // buffer size:131072
        this.queue = DisruptorQueue.independentPubSubInstance(2 << 17, false, consumers);
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
