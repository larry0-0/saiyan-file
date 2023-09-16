package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
import co.mgentertainment.file.dal.enums.ResourceTypeEnum;
import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.service.FileService;
import com.google.common.eventbus.AllowConcurrentEvents;
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

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(VideoUploadEvent event) {
        try {
            File videoFolder = event.getVideoFolder();
            if (videoFolder == null || !videoFolder.exists() || !videoFolder.isDirectory()) {
                log.error("视频文件夹{}不存在", videoFolder.getAbsolutePath());
                return;
            }
            fileService.folder2CloudStorage(videoFolder, ResourceTypeEnum.VIDEO);
            fileUploadRepository.updateUploadStatus(event.getUploadId(), UploadStatusEnum.COMPLETED);
        } catch (Exception e) {
            log.error("上传桶失败", e);
        }
    }
}
