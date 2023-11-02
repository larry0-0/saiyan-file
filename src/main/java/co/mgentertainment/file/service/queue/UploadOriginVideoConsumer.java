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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/24
 * @description UploadOriginVideoConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadOriginVideoConsumer extends AbstractDisruptorWorkConsumer<UploadOriginVideoParameter> {

    private final UploadWorkflowService uploadWorkflowService;
    private final FileService fileService;
    private final CutTrailerQueue cutTrailerQueue;
    private final CutShortVideoQueue cutShortVideoQueue;

    @Override
    public void consume(UploadOriginVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadOriginVideoConsumer参数异常");
            return;
        }
        String watermarkVideoPath = parameter.getWatermarkVideoPath();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("上传带水印原片");
            File watermarkVideo = FileUtil.exist(watermarkVideoPath) ? new File(watermarkVideoPath) : fileService.getWatermarkFile(uploadId);
            log.debug("(6)开始{}, uploadId:{}, origin video:{}", stopWatch.currentTaskName(), uploadId, watermarkVideo.getAbsolutePath());
            UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
            if (uploadResource == null || uploadResource.getRid() == null) {
                stopWatch.stop();
                log.debug("(6)结束{}, 数据错误，未找到uploadId:{}对应的rid", stopWatch.getLastTaskName(), uploadId);
                return;
            }
            Long rid = uploadResource.getRid();
            UploadSubStatusEnum subStatus = uploadResource.getHasTrailer().equals((byte) 1) ? UploadSubStatusEnum.CUTTING_TRAILER :
                    uploadResource.getHasShort().equals((byte) 1) ? UploadSubStatusEnum.CUTTING_SHORT :
                            UploadSubStatusEnum.END;
            uploadWorkflowService.uploadVideo2CloudStorage(watermarkVideo, VideoType.ORIGIN_VIDEO, subStatus, uploadResource.getFolder(), rid, uploadId);
            stopWatch.stop();
            log.debug("(6)结束{}, uploadId:{}, rid:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, rid, stopWatch.getLastTaskTimeMillis());
            boolean needTrailer = new Byte((byte) 1).equals(uploadResource.getHasTrailer());
            boolean needShort = new Byte((byte) 1).equals(uploadResource.getHasShort());
            if (needTrailer) {
                cutTrailerQueue.put(CutTrailerParameter.builder()
                        .uploadId(uploadId)
                        .watermarkVideoPath(watermarkVideo.getAbsolutePath())
                        .trailerDuration(uploadResource.getTrailerDuration())
                        .trailerStartFromProportion(uploadResource.getTrailerStartPos())
                        .rid(rid)
                        .build()
                );
            } else if (needShort) {
                cutShortVideoQueue.put(CutShortVideoParameter.builder()
                        .uploadId(uploadId)
                        .watermarkVideoPath(watermarkVideo.getAbsolutePath())
                        .shortVideoDuration(uploadResource.getShortDuration())
                        .shortVideoStartFromProportion(uploadResource.getShortStartPos())
                        .rid(rid)
                        .build()
                );
            } else {
                fileService.afterViceProcessComplete(uploadId);
            }
        } catch (Exception e) {
            log.error("UploadOriginVideoConsumer异常", e);
        }
    }
}
