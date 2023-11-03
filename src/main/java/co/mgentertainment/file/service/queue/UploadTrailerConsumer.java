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
 * @description UploadTrailerConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadTrailerConsumer extends AbstractDisruptorWorkConsumer<UploadTrailerParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(UploadTrailerParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadTrailerConsumer参数异常");
            return;
        }
        String trailerVideoPath = parameter.getTrailerVideoPath();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("上传预告片");
        log.debug("(8)开始{}, uploadId:{}, video:{}", stopWatch.currentTaskName(), uploadId, trailerVideoPath);
        uploadWorkflowService.uploadVideo2CloudStorage(new File(trailerVideoPath), VideoType.TRAILER, uploadId);
        stopWatch.stop();
        log.debug("(8)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
    }
}
