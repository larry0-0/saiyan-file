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
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/24
 * @description UploadTrailerConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UploadTrailerConsumer extends AbstractDisruptorWorkConsumer<UploadTrailerParameter> {

    private final UploadWorkflowService uploadWorkflowService;
    private final FileService fileService;
    private final CutShortVideoQueue cutShortVideoQueue;

    @Override
    public void consume(UploadTrailerParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadTrailerConsumer参数异常");
            return;
        }
        try {
            UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
            if (uploadResource == null || uploadResource.getRid() == null) {
                log.error("UploadTrailerConsumer参数异常,未找到uploadId:{}对应的rid", uploadId);
                return;
            }
            String trailerVideoPath = parameter.getTrailerVideoPath();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("上传预告片");
            log.debug("(8)开始{}, uploadId:{}, video:{}", stopWatch.currentTaskName(), uploadId, trailerVideoPath);
            File trailerVideo = FileUtil.exist(trailerVideoPath) ? new File(trailerVideoPath) : fileService.getTrailerFile(uploadId);
            boolean needShort = new Byte((byte) 1).equals(uploadResource.getHasShort());
            uploadWorkflowService.uploadVideo2CloudStorage(trailerVideo, VideoType.TRAILER,
                    needShort ? UploadSubStatusEnum.CUTTING_SHORT : UploadSubStatusEnum.END,
                    uploadResource.getFolder(), uploadResource.getRid(), uploadId);
            stopWatch.stop();
            log.debug("(8)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
            if (needShort) {
                File watermarkVideo = FileUtil.exist(parameter.getWatermarkVideoPath()) ? new File(parameter.getWatermarkVideoPath()) : fileService.getWatermarkFile(uploadId);
                cutShortVideoQueue.put(CutShortVideoParameter.builder()
                        .uploadId(uploadId)
                        .watermarkVideoPath(watermarkVideo.getAbsolutePath())
                        .shortVideoDuration(uploadResource.getShortDuration())
                        .shortVideoStartFromProportion(uploadResource.getShortStartPos())
                        .rid(uploadResource.getRid())
                        .build());
            } else {
                fileService.afterViceProcessComplete(uploadId);
            }
        } catch (Exception e) {
            log.error("UploadTrailerConsumer异常", e);
        }
    }
}
