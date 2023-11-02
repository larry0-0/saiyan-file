package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
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
 * @description CutShortVideoConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CutShortVideoConsumer extends AbstractDisruptorWorkConsumer<CutShortVideoParameter> {

    private final UploadWorkflowService uploadWorkflowService;
    private final FileService fileService;
    private final UploadShortVideoQueue uploadShortVideoQueue;

    @Override
    public void consume(CutShortVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("CutShortVideoConsumer参数异常");
            return;
        }
        String watermarkVideoPath = parameter.getWatermarkVideoPath();
        try {
            File watermarkVideo = FileUtil.exist(watermarkVideoPath) ? new File(watermarkVideoPath) : fileService.getWatermarkFile(uploadId);
            Integer shortVideoStartPos = parameter.getShortVideoStartFromProportion();
            Integer shortVideoDuration = parameter.getShortVideoDuration();

            if (shortVideoDuration == null) {
                UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
                if (uploadResource == null || uploadResource.getUploadId() == null) {
                    log.error("CutShortVideoConsumer参数异常,未找到uploadId:{}对应rid", uploadId);
                    return;
                }
                shortVideoDuration = uploadResource.getShortDuration();
                shortVideoStartPos = uploadResource.getShortStartPos();
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("剪切短视频");
            log.debug("(9)开始{}, uploadId:{}, 原片:{}", stopWatch.currentTaskName(), uploadId, watermarkVideo.getAbsolutePath());
            CuttingSetting setting = CuttingSetting.builder().shortVideoDuration(shortVideoDuration).shortVideoStartFromProportion(shortVideoStartPos).build();
            File shortVideo = uploadWorkflowService.cutVideo(watermarkVideo, VideoType.SHORT_VIDEO, setting, uploadId);
            if (!FileUtil.exist(shortVideo)) {
                log.error("(9)短视频文件不存在");
                return;
            }
            stopWatch.stop();
            log.debug("(9)结束{}, 视频位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), shortVideo.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            uploadShortVideoQueue.put(UploadShortVideoParameter.builder()
                    .uploadId(uploadId)
                    .shortVideoPath(shortVideo.getAbsolutePath())
                    .rid(parameter.getRid())
                    .build());
        } catch (Throwable t) {
            log.error("CutShortVideoConsumer异常", t);
        }
    }
}
