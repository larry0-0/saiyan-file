package co.mgentertainment.file.service.queue;

import co.mgentertainment.common.utils.queue.DisruptorQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description CaptureAndUploadCoverQueue
 */
@Component
@RequiredArgsConstructor
public class CaptureAndUploadCoverQueue<T> implements Queueable<T>, InitializingBean, DisposableBean {

    private final CaptureAndUploadCoverConsumer captureAndUploadCoverConsumer;
    private DisruptorQueue<T> queue;


    @Override
    public void afterPropertiesSet() {
        // buffer size:131072
        this.queue = DisruptorQueue.independentPubSubInstance(2 << 17, false, captureAndUploadCoverConsumer);
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
