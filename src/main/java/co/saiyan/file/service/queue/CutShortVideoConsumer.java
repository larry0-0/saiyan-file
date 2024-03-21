package co.saiyan.file.service.queue;

import cn.hutool.core.date.StopWatch;
import co.saiyan.common.model.media.VideoType;
import co.saiyan.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.saiyan.file.service.UploadWorkflowService;
import co.saiyan.file.service.config.CuttingSetting;
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

    @Override
    public void consume(CutShortVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("CutShortVideoConsumer参数异常");
            return;
        }
        String watermarkVideoPath = parameter.getWatermarkVideoPath();
        Integer shortVideoStartPos = parameter.getShortVideoStartFromProportion();
        Integer shortVideoDuration = parameter.getShortVideoDuration();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("剪切短视频");
        log.debug("(9)开始{}, uploadId:{}, 原片:{}", stopWatch.currentTaskName(), uploadId, watermarkVideoPath);
        CuttingSetting setting = CuttingSetting.builder().shortVideoDuration(shortVideoDuration).shortVideoStartFromProportion(shortVideoStartPos).build();
        File watermarkVideo = watermarkVideoPath == null ? null : new File(watermarkVideoPath);
        uploadWorkflowService.cutVideo(watermarkVideo, VideoType.SHORT_VIDEO, setting, uploadId);
        stopWatch.stop();
        log.debug("(9)结束{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }
}
