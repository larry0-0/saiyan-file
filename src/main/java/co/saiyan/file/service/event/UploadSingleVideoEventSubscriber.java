package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEventSubscriber;
import co.saiyan.common.model.media.VideoType;
import co.saiyan.file.service.queue.*;
import co.saiyan.file.service.queue.*;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadSingleVideoEventSubscriber
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UploadSingleVideoEventSubscriber extends AbstractEventSubscriber<UploadSingleVideoEvent> {

    private final UploadOriginVideoQueue uploadOriginVideoQueue;
    private final UploadTrailerQueue uploadTrailerQueue;
    private final UploadShortVideoQueue uploadShortVideoQueue;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(UploadSingleVideoEvent event) {
        VideoType type = event.getType();
        try {
            switch (type) {
                case ORIGIN_VIDEO:
                    uploadOriginVideoQueue.put(
                            UploadOriginVideoParameter.builder()
                                    .uploadId(event.getUploadId())
                                    .watermarkVideoPath(event.getVideoPath())
                                    .build());
                    break;
                case TRAILER:
                    uploadTrailerQueue.put(
                            UploadTrailerParameter.builder()
                                    .uploadId(event.getUploadId())
                                    .trailerVideoPath(event.getVideoPath())
                                    .build());
                    break;
                case SHORT_VIDEO:
                    uploadShortVideoQueue.put(
                            UploadShortVideoParameter.builder()
                                    .uploadId(event.getUploadId())
                                    .shortVideoPath(event.getVideoPath())
                                    .build());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("上传单个视频事件异常", e);
        }
    }
}
