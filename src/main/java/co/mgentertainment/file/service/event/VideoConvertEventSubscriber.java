package co.mgentertainment.file.service.event;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.service.UploadWorkflowService;
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
 * @description VideoConvertEventSubscriber
 */
@Component
@Slf4j
public class VideoConvertEventSubscriber extends AbstractEventSubscriber<VideoConvertEvent> {

    @Resource
    private UploadWorkflowService uploadWorkflowService;

    @Resource
    private AsyncEventBus eventBus;


    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(VideoConvertEvent event) {
        try {
            Long uploadId = event.getUploadId();
            File originVideo = event.getOriginVideo();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("转码");
            log.debug("(2.1)开始{}, uploadId:{}, 视频位置:{}", stopWatch.getLastTaskName(), uploadId, originVideo.getAbsolutePath());
            File m3u8File = uploadWorkflowService.convertVideo(originVideo, uploadId);
            stopWatch.stop();
            log.debug("(2.2)结束{}, 已转码位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), m3u8File.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            if (m3u8File == null || !m3u8File.exists()) {
                return;
            }
            eventBus.post(
                    VideoUploadEvent.builder()
                            .uploadId(event.getUploadId())
                            .processedVideo(m3u8File)
                            .originVideo(event.getOriginVideo())
                            .videoType(VideoType.FEATURE_FILM)
                            .cuttingSetting(event.getCuttingSetting())
                            .appName(event.getAppName())
                            .build());
        } catch (Exception e) {
            log.error("转码事件异常", e);
        }
    }
}
