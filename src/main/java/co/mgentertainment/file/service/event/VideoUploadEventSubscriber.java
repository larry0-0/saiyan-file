package co.mgentertainment.file.service.event;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.VideoType;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.Optional;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description VideoUploadEventSubscriber
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VideoUploadEventSubscriber extends AbstractEventSubscriber<VideoUploadEvent> {

    private final UploadWorkflowService uploadWorkflowService;
    private final AsyncEventBus eventBus;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(VideoUploadEvent event) {
        try {
            File folderToUpload = event.getProcessedVideo().getParentFile();
            if (folderToUpload == null || !folderToUpload.exists() || !folderToUpload.isDirectory()) {
                log.error("待上传的视频文件夹{}不存在", folderToUpload.getAbsolutePath());
                return;
            }
            Long uploadId = event.getUploadId();
            File originVideo = event.getOriginVideo();
            File processedVideo = event.getProcessedVideo();
            String subDirName = Optional.ofNullable(event.getSubDirName()).orElse(DateUtils.format(new Date(), DateUtils.FORMAT_YYYYMMDD));
            VideoType videoType = event.getVideoType();
            CuttingSetting cuttingSetting = event.getCuttingSetting();
            if (videoType == VideoType.FEATURE_FILM) {
                boolean isLastStep = cuttingSetting == null || (cuttingSetting.getTrailerDuration() == null && cuttingSetting.getShortVideoDuration() == null);
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("上传正片并添加资源记录");
                log.debug("(3.1)开始{}, uploadId:{}, 正片:{}", stopWatch.currentTaskName(), uploadId, processedVideo.getAbsolutePath());
                Long rid = uploadWorkflowService.uploadFilmFolder2CloudStorage(processedVideo.getParentFile(), subDirName, originVideo, event.getAppCode(), isLastStep, uploadId);
                stopWatch.stop();
                log.debug("(3.2)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
                if (rid == null) {
                    log.error("(3)rid返回为空");
                    return;
                }
                if (isLastStep) {
                    return;
                }
                VideoType nextVideoType = cuttingSetting.getTrailerDuration() != null ? VideoType.TRAILER : VideoType.SHORT_VIDEO;
                eventBus.post(
                        VideoCutEvent.builder()
                                .uploadId(event.getUploadId())
                                .originVideo(event.getOriginVideo())
                                .cuttingSetting(cuttingSetting)
                                .rid(rid)
                                .type(nextVideoType)
                                .build());
            } else {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("上传" + (videoType == VideoType.TRAILER ? "预告片" : "短视频"));
                log.debug("(5.1)开始{}, uploadId:{}, video:{}", stopWatch.currentTaskName(), uploadId, processedVideo.getAbsolutePath());
                boolean isLastStep = (videoType == VideoType.TRAILER && cuttingSetting.getShortVideoDuration() == null) || (videoType == VideoType.SHORT_VIDEO && cuttingSetting.getTrailerDuration() == null);
                uploadWorkflowService.uploadVideo2CloudStorage(event.getProcessedVideo(), videoType, isLastStep, event.getRid(), subDirName, event.getUploadId());
                stopWatch.stop();
                log.debug("(5.2)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
                if (isLastStep) {
                    return;
                }
                VideoType nextVideoType = cuttingSetting.getShortVideoDuration() != null ? VideoType.SHORT_VIDEO : VideoType.TRAILER;
                eventBus.post(
                        VideoCutEvent.builder()
                                .uploadId(event.getUploadId())
                                .originVideo(event.getOriginVideo())
                                .cuttingSetting(cuttingSetting)
                                .type(nextVideoType)
                                .build());
            }
        } catch (Exception e) {
            log.error("上传事件异常", e);
        }
    }
}
