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
 * @description UploadFilmQueue
 */
@Component
@RequiredArgsConstructor
public class UploadFilmQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {

    private final ThreadPoolExecutor fileUploadThreadPool;
    private final UploadFilmConsumer uploadFilmConsumer;

    private DisruptorQueue<T> queue;

    @Override
    public void afterPropertiesSet() throws Exception {
        // buffer size:131072
        this.queue = DisruptorQueue.independentPubSubInstance(2 << 17, false, fileUploadThreadPool, uploadFilmConsumer);
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
