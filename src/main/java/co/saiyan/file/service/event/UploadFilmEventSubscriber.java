package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEventSubscriber;
import co.saiyan.file.service.queue.UploadFilmParameter;
import co.saiyan.file.service.queue.UploadFilmQueue;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadFilmEventSubscriber
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UploadFilmEventSubscriber extends AbstractEventSubscriber<UploadFilmEvent> {

    private final UploadFilmQueue uploadFilmQueue;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(UploadFilmEvent event) {
        try {
            uploadFilmQueue.put(UploadFilmParameter.builder()
                    .uploadId(event.getUploadId())
                    .originVideoPath(event.getOriginVideoPath())
                    .processedVideoPath(event.getProcessedVideoPath())
                    .appCode(event.getAppCode())
                    .build());
        } catch (Exception e) {
            log.error("上传正片事件异常", e);
        }
    }
}
