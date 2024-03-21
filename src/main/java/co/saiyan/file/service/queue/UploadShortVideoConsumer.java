package co.saiyan.file.service.queue;

import cn.hutool.core.date.StopWatch;
import co.saiyan.common.model.media.VideoType;
import co.saiyan.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.saiyan.file.service.UploadWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/24
 * @description UploadShortVideoConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadShortVideoConsumer extends AbstractDisruptorWorkConsumer<UploadShortVideoParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(UploadShortVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadShortVideoConsumer参数异常");
            return;
        }
        String shortVideoPath = parameter.getShortVideoPath();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("上传短视频");
        log.debug("(10)开始{}, uploadId:{}, video:{}", stopWatch.currentTaskName(), uploadId, shortVideoPath);
        uploadWorkflowService.uploadVideo2CloudStorage(new File(shortVideoPath), VideoType.SHORT_VIDEO, uploadId);
        stopWatch.stop();
        log.debug("(10)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
    }
}
