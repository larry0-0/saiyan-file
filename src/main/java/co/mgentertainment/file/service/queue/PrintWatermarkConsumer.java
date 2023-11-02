package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;

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
    private final FileService fileService;
    private final UploadOriginVideoQueue uploadOriginVideoQueue;

    @Override
    public void consume(PrintWatermarkParameter request) {
        Long uploadId = request.getUploadId();
        if (uploadId == null) {
            log.error("PrintWatermarkConsumer参数异常");
            return;
        }
        String newOriginVideoPath = request.getNewOriginVideoPath();
        try {
            File originVideo = FileUtil.exist(newOriginVideoPath) ? new File(newOriginVideoPath) : fileService.getViceOriginFile(uploadId);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("打水印");
            log.debug("(5)开始{}, uploadId:{}, 视频位置:{}", stopWatch.currentTaskName(), uploadId, newOriginVideoPath);
            File watermarkVideo = uploadWorkflowService.printWatermark(originVideo, uploadId);
            stopWatch.stop();
            if (!FileUtil.exist(watermarkVideo)) {
                log.error("(5)打水印失败");
            } else {
                log.debug("(5)结束{}, 水印视频位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), watermarkVideo.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            }
            uploadOriginVideoQueue.put(UploadOriginVideoParameter.builder()
                    .uploadId(uploadId)
                    .watermarkVideoPath(Optional.ofNullable(watermarkVideo).orElse(originVideo).getAbsolutePath())
                    .build());
        } catch (Exception e) {
            log.error("VideoPrintWatermarkConsumer error", e);
        }
    }
}
