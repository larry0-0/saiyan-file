package co.mgentertainment.file.service.event;

import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.dal.enums.ResourceTypeEnum;
import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.FileService;
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
 * @description VideoUploadEventSubscriber
 */
@Component
@Slf4j
public class VideoUploadEventSubscriber extends AbstractEventSubscriber<VideoUploadEvent> {

    @Resource
    private FileService fileService;
    @Resource
    private FileUploadRepository fileUploadRepository;
    @Resource
    private FfmpegService ffmpegService;
    @Resource
    private AsyncEventBus eventBus;

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
            if (event.getVideoType() == VideoType.FEATURE_FILM) {
                log.debug("上传正片并生成rid，状态转换：UPLOADING->TRAILER_CUTTING_AND_UPLOADING，下一步剪切预告片");
                Integer duration = ffmpegService.getMediaDuration(event.getOriginVideo());
                Long rid = fileService.media2CloudStorage(event.getProcessedVideo(), ResourceTypeEnum.VIDEO, event.getAppName(), duration);
                // 正片上传成功就填充rid
                fileUploadRepository.updateUploadStatus(event.getUploadId(), UploadStatusEnum.TRAILER_CUTTING_AND_UPLOADING, rid);
                eventBus.post(
                        VideoCutEvent.builder()
                                .uploadId(event.getUploadId())
                                .originVideo(event.getOriginVideo())
                                .cuttingSetting(event.getCuttingSetting())
                                .rid(rid)
                                .build());
            } else {
                log.debug("上传预告片，状态转换：TRAILER_CUTTING_AND_UPLOADING->COMPLETED");
                fileService.uploadLocalTrailUnderResource(event.getRid(), event.getProcessedVideo());
                fileUploadRepository.updateUploadStatus(event.getUploadId(), UploadStatusEnum.COMPLETED, event.getRid());
                deleteCompletedVideoFolder(event.getOriginVideo());
            }
        } catch (Exception e) {
            log.error("上传事件异常", e);
        }
    }

    private void deleteCompletedVideoFolder(File originVideo) {
        File folderToDelete = originVideo.getParentFile();
        if (folderToDelete != null && folderToDelete.isDirectory()) {
            log.debug("删除转码后的视频文件夹{}", folderToDelete.getAbsolutePath());
            FileUtil.del(folderToDelete.getAbsolutePath());
        }
    }
}
