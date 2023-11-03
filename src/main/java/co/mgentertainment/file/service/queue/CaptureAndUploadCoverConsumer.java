package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
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
 * @description CaptureAndUploadCoverConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CaptureAndUploadCoverConsumer extends AbstractDisruptorWorkConsumer<CaptureAndUploadCoverParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(CaptureAndUploadCoverParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("CaptureAndUploadCoverConsumer参数异常");
            return;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("截取并上传封面");
        String originVideoPath = parameter.getOriginVideoPath();
        log.debug("(4)开始{}, uploadId:{}, video:{}", stopWatch.currentTaskName(), uploadId, originVideoPath);
        uploadWorkflowService.captureAndUploadScreenshot(new File(originVideoPath), uploadId);
        stopWatch.stop();
        log.debug("(4)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
    }
}
