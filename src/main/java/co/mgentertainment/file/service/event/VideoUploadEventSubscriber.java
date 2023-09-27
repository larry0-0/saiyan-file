package co.mgentertainment.file.service.event;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.file.service.UploadWorkflowService;
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
            if (event.getVideoType() == VideoType.FEATURE_FILM) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("上传正片并添加资源记录");
                log.debug("(3.1)开始{}, uploadId:{}, 正片:{}", stopWatch.getLastTaskName(), uploadId, processedVideo.getAbsolutePath());
                Long rid = uploadWorkflowService.uploadFilmFolder2CloudStorage(processedVideo.getParentFile(), subDirName, originVideo, event.getAppName(), uploadId);
                stopWatch.stop();
                log.debug("(3.2)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
                if (rid == null) {
                    return;
                }
                eventBus.post(
                        VideoCutEvent.builder()
                                .uploadId(event.getUploadId())
                                .originVideo(event.getOriginVideo())
                                .cuttingSetting(event.getCuttingSetting())
                                .rid(rid)
                                .build());
            } else {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("上传预告片");
                log.debug("(5.1)开始{}, uploadId:{}, 预告片:{}", stopWatch.getLastTaskName(), uploadId, processedVideo.getAbsolutePath());
                uploadWorkflowService.uploadTrailer2CloudStorage(event.getProcessedVideo(), event.getRid(), subDirName, event.getUploadId());
                stopWatch.stop();
                log.debug("(5.2)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
            }
        } catch (Exception e) {
            log.error("上传事件异常", e);
        }
    }
}
