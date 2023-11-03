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
 * @description PrintWatermarkConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrintWatermarkConsumer extends AbstractDisruptorWorkConsumer<PrintWatermarkParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(PrintWatermarkParameter request) {
        Long uploadId = request.getUploadId();
        if (uploadId == null) {
            log.error("PrintWatermarkConsumer参数异常");
            return;
        }
        String newOriginVideoPath = request.getNewOriginVideoPath();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("打水印");
        log.debug("(5)开始{}, uploadId:{}, 视频位置:{}", stopWatch.currentTaskName(), uploadId, newOriginVideoPath);
        uploadWorkflowService.printWatermark(new File(newOriginVideoPath), uploadId);
        stopWatch.stop();
        log.debug("(5)结束{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }
}
