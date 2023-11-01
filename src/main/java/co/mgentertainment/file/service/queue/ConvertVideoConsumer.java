package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ConvertVideoConsumer extends AbstractDisruptorWorkConsumer<ConvertVideoParameter> {

    private final UploadWorkflowService uploadWorkflowService;
    private final FileService fileService;
    private final UploadFilmQueue uploadFilmQueue;

    @Override
    public void consume(ConvertVideoParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("ConvertVideoConsumer参数异常");
            return;
        }
        String originVideoPath = parameter.getOriginVideoPath();
        try {
            File originVideo = FileUtil.exist(originVideoPath) ? new File(originVideoPath) : fileService.getMainOriginFile(uploadId);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("转码");
            log.debug("(2)开始{}, uploadId:{}, 视频位置:{}", stopWatch.currentTaskName(), uploadId, originVideoPath);
            File m3u8File = uploadWorkflowService.convertVideo(originVideo, uploadId);
            stopWatch.stop();
            if (!FileUtil.exist(m3u8File)) {
                log.error("(2)转码失败");
                return;
            }
            log.debug("(2)结束{}, 已转码位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), m3u8File.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            uploadFilmQueue.put(UploadFilmParameter.builder()
                    .uploadId(uploadId)
                    .originVideoPath(originVideoPath)
                    .processedVideoPath(m3u8File.getAbsolutePath())
                    .appCode(parameter.getAppCode())
                    .build());
        } catch (Exception e) {
            log.error("ConvertVideoConsumer异常", e);
        }
    }
}
