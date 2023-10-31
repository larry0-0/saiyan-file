package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.dto.UploadResourceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/24
 * @description CaptureAndUploadCoverConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CaptureAndUploadCoverConsumer extends AbstractDisruptorWorkConsumer<CaptureAndUploadCoverParameter> {

    private final FileService fileService;
    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(CaptureAndUploadCoverParameter parameter) {
        Long uploadId = parameter.getUploadId();
        String originVideoPath = parameter.getOriginVideoPath();
        if (uploadId == null) {
            log.error("CaptureAndUploadCoverConsumer参数异常");
            return;
        }
        try {
            UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
            if (uploadResource == null || uploadResource.getUploadId() == null) {
                log.error("CaptureAndUploadCoverConsumer参数异常,未找到uploadId:{}", uploadId);
                return;
            }
            File originVideo = FileUtil.exist(originVideoPath) ? new File(originVideoPath) : fileService.getMainOriginFile(uploadId);
            boolean hasCover = new Byte((byte) 1).equals(uploadResource.getHasCover());
            if (hasCover) {
                Long rid = uploadResource.getRid();
                if (rid != null) {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start("截取并上传封面");
                    log.debug("(4)开始{}, uploadId:{}, video:{}", stopWatch.currentTaskName(), uploadId, originVideoPath);
                    String cloudSubDirName = uploadResource.getFolder();
                    uploadWorkflowService.captureAndUploadScreenshot(originVideo, cloudSubDirName, rid, uploadId);
                    stopWatch.stop();
                    log.debug("(4)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
                }
            }
            fileService.afterMainProcessComplete(uploadId, originVideo);
        } catch (Exception e) {
            log.error("CaptureAndUploadCoverConsumer异常", e);
        }
    }
}
