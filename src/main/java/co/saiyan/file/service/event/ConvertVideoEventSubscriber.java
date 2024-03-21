package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEventSubscriber;
import co.saiyan.file.service.queue.ConvertVideoParameter;
import co.saiyan.file.service.queue.ConvertVideoQueue;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description ConvertVideoEventSubscriber
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ConvertVideoEventSubscriber extends AbstractEventSubscriber<ConvertVideoEvent> {

    private final ConvertVideoQueue convertVideoQueue;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(ConvertVideoEvent event) {
        try {
            convertVideoQueue.put(ConvertVideoParameter.builder()
                    .uploadId(event.getUploadId())
                    .originVideoPath(event.getOriginVideoPath())
                    .isShortVideo(event.getIsShortVideo())
                    .build());
        } catch (Exception e) {
            log.error("视频转码事件异常", e);
        }
    }
}
