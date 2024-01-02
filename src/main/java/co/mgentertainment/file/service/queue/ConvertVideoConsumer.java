package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.UploadWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/24
 * @description ConvertVideoConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConvertVideoConsumer extends AbstractDisruptorWorkConsumer<ConvertVideoParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(ConvertVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("ConvertVideoConsumer参数异常");
            return;
        }
        String originVideoPath = parameter.getOriginVideoPath();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("转码");
        log.debug("(2)开始{}, uploadId:{}, 视频位置:{}", stopWatch.currentTaskName(), uploadId, originVideoPath);
        uploadWorkflowService.convertVideo(new File(originVideoPath), uploadId, BooleanUtils.isTrue(parameter.getIsShortVideo()));
        stopWatch.stop();
        log.debug("(2)结束{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
    }
}
