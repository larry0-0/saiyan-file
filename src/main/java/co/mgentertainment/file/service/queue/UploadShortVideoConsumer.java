package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.model.media.UploadSubStatusEnum;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.dto.UploadResourceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/24
 * @description UploadShortVideoConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UploadShortVideoConsumer extends AbstractDisruptorWorkConsumer<UploadShortVideoParameter> {

    private final UploadWorkflowService uploadWorkflowService;
    private final FileService fileService;

    @Override
    public void consume(UploadShortVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadShortVideoConsumer参数异常");
            return;
        }
        try {
            String shortVideoPath = parameter.getShortVideoPath();
            File shortVideo = FileUtil.exist(shortVideoPath) ? new File(shortVideoPath) : fileService.getShortVideoFile(uploadId);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("上传短视频");
            log.debug("(10)开始{}, uploadId:{}, video:{}", stopWatch.currentTaskName(), uploadId, shortVideo.getAbsolutePath());
            String subDirName = parameter.getCloudSubDirName();
            Long rid = parameter.getRid();
            if (StringUtils.isBlank(subDirName) || rid == null) {
                UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
                if (uploadResource == null || uploadResource.getUploadId() == null) {
                    log.error("UploadShortVideoConsumer参数异常，未找到uploadId:{}对应的rid", uploadId);
                    return;
                }
                rid = uploadResource.getRid();
                subDirName = uploadResource.getFolder();
            }
            uploadWorkflowService.uploadVideo2CloudStorage(shortVideo, VideoType.SHORT_VIDEO, UploadSubStatusEnum.END,
                    subDirName, rid, uploadId);
            stopWatch.stop();
            log.debug("(10)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
            fileService.afterViceProcessComplete(uploadId);
        } catch (Exception e) {
            log.error("UploadShortVideoConsumer异常", e);
        }
    }
}
