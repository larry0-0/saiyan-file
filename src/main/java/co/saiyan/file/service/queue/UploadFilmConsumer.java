package co.saiyan.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
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
 * @description UploadFilmConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadFilmConsumer extends AbstractDisruptorWorkConsumer<UploadFilmParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    @Override
    public void consume(UploadFilmParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadFilmConsumer参数异常");
            return;
        }
        String originVideoPath = parameter.getOriginVideoPath();
        File processedVideo = new File(parameter.getProcessedVideoPath());
        String appCode = parameter.getAppCode();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("上传正片并添加资源记录");
        File folderToUpload = FileUtil.exist(processedVideo) ? processedVideo.getParentFile() : null;
        log.debug("(3)开始{}, uploadId:{}, 正片目录:{}", stopWatch.currentTaskName(), uploadId, folderToUpload.getAbsolutePath());
        uploadWorkflowService.uploadFilmFolder2CloudStorage(folderToUpload, new File(originVideoPath), appCode, uploadId);
        stopWatch.stop();
        log.debug("(3)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
    }
}
