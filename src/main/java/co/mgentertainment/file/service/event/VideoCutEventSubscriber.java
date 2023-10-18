package co.mgentertainment.file.service.event;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.utils.MediaHelper;
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
        VideoType videoType = event.getType();
        try {
            File watermarkVideo = event.getWatermarkVideo();
            CuttingSetting cuttingSetting = event.getCuttingSetting();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("剪切" + (videoType == VideoType.TRAILER ? "预告片" : "短视频"));
            log.debug("(4.1)开始{}, uploadId:{}, 水印片:{}", stopWatch.currentTaskName(), event.getUploadId(), watermarkVideo.getAbsolutePath());
            File video = uploadWorkflowService.cutVideo(watermarkVideo, videoType, cuttingSetting, event.getUploadId());
            if (video == null) {
                log.error("(4){}文件不存在", videoType == VideoType.TRAILER ? "预告片" : "短视频");
                return;
            }
            stopWatch.stop();
            log.debug("(4.2)结束{}, 视频位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), video.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            // 清空剪切设置便于后续判断是否需要处理剪切
            MediaHelper.clearCutSettingAfterCut(videoType, cuttingSetting);
            eventBus.post(
                    VideoUploadEvent.builder()
                            .uploadId(event.getUploadId())
                            .processedVideo(video)
                            .originVideo(event.getOriginVideo())
                            .watermarkVideo(watermarkVideo)
                            .videoType(videoType)
                            .rid(event.getRid())
                            .cuttingSetting(cuttingSetting)
                            .build());
        } catch (Exception e) {
            log.error("切" + (videoType == VideoType.TRAILER ? "预告片" : "短视频") + "事件异常", e);
        }
    }
}
