package co.mgentertainment.file.service.queue;

import co.mgentertainment.common.utils.queue.DisruptorQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description ConvertVideoQueue
 */
@Component
@RequiredArgsConstructor
public class ConvertVideoQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {

    private final ConvertVideoConsumer convertVideoConsumer;

    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() {
        // buffer size:131072
        this.queue = (DisruptorQueue<T>) new DisruptorQueue<>(2 << 17, false, convertVideoConsumer);
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
