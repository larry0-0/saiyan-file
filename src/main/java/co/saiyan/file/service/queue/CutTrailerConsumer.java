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
 * @description CutTrailerConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CutTrailerConsumer extends AbstractDisruptorWorkConsumer<CutTrailerParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(CutTrailerParameter parameter) {
        Long uploadId = parameter.getUploadId();
        String watermarkVideoPath = parameter.getWatermarkVideoPath();
        Integer trailerDuration = parameter.getTrailerDuration();
        Integer trailerStartFromProportion = parameter.getTrailerStartFromProportion();
        if (uploadId == null) {
            log.error("CutTrailerConsumer参数异常");
            return;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("剪切预告片");
        log.debug("(7)开始{}, uploadId:{}, 原片:{}", stopWatch.currentTaskName(), uploadId, watermarkVideoPath);
        File watermarkVideo = watermarkVideoPath == null ? null : new File(watermarkVideoPath);
        CuttingSetting setting = CuttingSetting.builder().trailerDuration(trailerDuration).trailerStartFromProportion(trailerStartFromProportion).build();
        uploadWorkflowService.cutVideo(watermarkVideo, VideoType.TRAILER, setting, uploadId);
        stopWatch.stop();
        log.debug("(7)结束{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }
}
