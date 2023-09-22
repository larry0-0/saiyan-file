package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
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
            log.debug("转码，状态转换：CONVERTING->UPLOADING");
            File m3u8File = ffmpegService.mediaConvert(event.getOriginVideo());
            FileUploadDO uploadDO = new FileUploadDO();
            uploadDO.setUploadId(event.getUploadId());
            uploadDO.setStatus(Integer.valueOf(UploadStatusEnum.UPLOADING.getValue()).shortValue());
            fileUploadRepository.updateFileUploadByPrimaryKey(uploadDO);
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
