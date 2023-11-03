package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.UploadWorkflowService;
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

    @Override
    public void consume(UploadOriginVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadOriginVideoConsumer参数异常");
            return;
        }
        String watermarkVideoPath = parameter.getWatermarkVideoPath();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("上传带水印原片");
        log.debug("(6)开始{}, uploadId:{}, origin video:{}", stopWatch.currentTaskName(), uploadId, watermarkVideoPath);
        uploadWorkflowService.uploadVideo2CloudStorage(new File(watermarkVideoPath), VideoType.ORIGIN_VIDEO, uploadId);
        stopWatch.stop();
        log.debug("(6)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
    }
}
