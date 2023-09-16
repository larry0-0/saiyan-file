package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.service.FfmpegService;
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
    private FfmpegService ffmpegService;

    @Resource
    private FileUploadRepository fileUploadRepository;

    @Resource
    private AsyncEventBus eventBus;


    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(VideoConvertEvent event) {
        try {
            ffmpegService.mediaConvert(new File(event.getVideoFilePath()));
            fileUploadRepository.updateUploadStatus(event.getUploadId(), UploadStatusEnum.TO_UPLOAD);
            File video = new File(event.getVideoFilePath());
            eventBus.post(VideoUploadEvent.builder().uploadId(event.getUploadId()).videoFolder(video.getParentFile()).build());
        } catch (Exception e) {
            log.error("转码失败", e);
        }
    }
}
