package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.config.VideoType;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description VideoCutEventSubscriber
 */
@Component
@Slf4j
public class VideoCutEventSubscriber extends AbstractEventSubscriber<VideoCutEvent> {

    @Resource
    private FfmpegService ffmpegService;

    @Resource
    private AsyncEventBus eventBus;


    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(VideoCutEvent event) {
        try {
            log.debug("剪切预告片，无状态转换，下一步上传预告片");
            File mp4File = ffmpegService.mediaCut(event.getOriginVideo(), event.getCuttingSetting());
            eventBus.post(
                    VideoUploadEvent.builder()
                            .uploadId(event.getUploadId())
                            .processedVideo(mp4File)
                            .videoType(VideoType.TRAILER)
                            .rid(event.getRid())
                            .build());
        } catch (Exception e) {
            log.error("转码失败", e);
        }
    }
}
