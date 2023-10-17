package co.mgentertainment.file.service.event;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.UploadWorkflowService;
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
            stopWatch.start("打水印");
            log.debug("(2.1)开始{}, uploadId:{}, 视频位置:{}", stopWatch.currentTaskName(), uploadId, originVideo.getAbsolutePath());
            File watermarkFile = uploadWorkflowService.printWatermark(originVideo, uploadId);
            stopWatch.stop();
            if (watermarkFile == null || !watermarkFile.exists()) {
                log.error("(2)打水印失败");
                return;
            } else {
                log.debug("(2.1)结束{}, 已转码位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), watermarkFile.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            }

            stopWatch.start("转码");
            log.debug("(2.2)开始{}, uploadId:{}, 视频位置:{}", stopWatch.currentTaskName(), uploadId, originVideo.getAbsolutePath());
            File m3u8File = uploadWorkflowService.convertVideo(originVideo, uploadId, true);
            stopWatch.stop();
            if (m3u8File == null || !m3u8File.exists()) {
                log.error("(2)转码失败");
                return;
            } else {
                log.debug("(2.2)结束{}, 已转码位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), m3u8File.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            }
            eventBus.post(
                    VideoUploadEvent.builder()
                            .uploadId(event.getUploadId())
                            .processedVideo(m3u8File)
                            .watermarkVideo(watermarkFile)
                            .originVideo(event.getOriginVideo())
                            .videoType(VideoType.FEATURE_FILM)
                            .cuttingSetting(event.getCuttingSetting())
                            .appCode(event.getAppCode())
                            .build());
        } catch (Exception e) {
            log.error("转码事件异常", e);
        }
    }
}
