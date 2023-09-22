package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.config.VideoType;
import co.mgentertainment.file.service.dto.MediaCutResultDTO;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
            MediaCutResultDTO cutResult = ffmpegService.mediaCut(event.getOriginVideo(), event.getCuttingSetting());
            log.debug("剪切预告片完成，更新资源时长");
            eventBus.post(
                    VideoUploadEvent.builder()
                            .uploadId(event.getUploadId())
                            .processedVideo(cutResult.getTrailerFile())
                            .originVideo(event.getOriginVideo())
                            .videoType(VideoType.TRAILER)
                            .rid(event.getRid())
                            .build());
        } catch (Exception e) {
            log.error("切预告片事件异常", e);
        }
    }
}
