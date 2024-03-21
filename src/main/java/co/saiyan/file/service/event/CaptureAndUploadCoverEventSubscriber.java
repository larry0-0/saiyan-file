package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEventSubscriber;
import co.saiyan.file.service.queue.CaptureAndUploadCoverParameter;
import co.saiyan.file.service.queue.CaptureAndUploadCoverQueue;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CaptureAndUploadCoverEventSubscriber
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CaptureAndUploadCoverEventSubscriber extends AbstractEventSubscriber<CaptureAndUploadCoverEvent> {

    private final CaptureAndUploadCoverQueue captureAndUploadCoverQueue;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(CaptureAndUploadCoverEvent event) {
        try {
            captureAndUploadCoverQueue.put(CaptureAndUploadCoverParameter.builder()
                    .uploadId(event.getUploadId())
                    .originVideoPath(event.getOriginVideoPath())
                    .build());
        } catch (Exception e) {
            log.error("截封面并上传事件异常", e);
        }
    }
}
