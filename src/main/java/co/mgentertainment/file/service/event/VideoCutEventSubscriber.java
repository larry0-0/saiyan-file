package co.mgentertainment.file.service.event;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.VideoType;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description VideoCutEventSubscriber
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VideoCutEventSubscriber extends AbstractEventSubscriber<VideoCutEvent> {

    private final UploadWorkflowService uploadWorkflowService;
    private final AsyncEventBus eventBus;


    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(VideoCutEvent event) {
        try {
            File originVideo = event.getOriginVideo();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("剪切预告片");
            log.debug("(4.1)开始{}, uploadId:{}, 原始片:{}", stopWatch.currentTaskName(), event.getUploadId(), originVideo.getAbsolutePath());
            File trailerFile = uploadWorkflowService.cutVideo(event.getOriginVideo(), event.getCuttingSetting(), event.getUploadId());
            if (trailerFile == null) {
                log.error("(4)预告片文件不存在");
                return;
            }
            stopWatch.stop();
            log.debug("(4.2)结束{}, 预告片位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), trailerFile.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            eventBus.post(
                    VideoUploadEvent.builder()
                            .uploadId(event.getUploadId())
                            .processedVideo(trailerFile)
                            .originVideo(event.getOriginVideo())
                            .videoType(VideoType.TRAILER)
                            .rid(event.getRid())
                            .build());
        } catch (Exception e) {
            log.error("切预告片事件异常", e);
        }
    }
}
