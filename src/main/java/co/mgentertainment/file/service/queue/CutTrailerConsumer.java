package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class CutTrailerConsumer extends AbstractDisruptorWorkConsumer<CutTrailerParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    private final UploadTrailerQueue uploadTrailerQueue;

    @Override
    public void consume(CutTrailerParameter parameter) {
        Long uploadId = parameter.getUploadId();
        String watermarkVideoPath = parameter.getWatermarkVideoPath();
        Integer trailerDuration = parameter.getTrailerDuration();
        Integer trailerStartFromProportion = parameter.getTrailerStartFromProportion();
        Long rid = parameter.getRid();
        if (StringUtils.isBlank(watermarkVideoPath) || !new File(watermarkVideoPath).exists() || trailerDuration == null || trailerStartFromProportion == null || rid == null) {
            log.error("CutTrailerConsumer参数异常");
            return;
        }
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("剪切预告片");
            log.debug("(7)开始{}, uploadId:{}, 原片:{}", stopWatch.currentTaskName(), uploadId, watermarkVideoPath);
            File watermarkVideo = new File(watermarkVideoPath);
            CuttingSetting setting = CuttingSetting.builder().trailerDuration(trailerDuration).trailerStartFromProportion(trailerStartFromProportion).build();
            File trailerVideo = uploadWorkflowService.cutVideo(watermarkVideo, VideoType.TRAILER, setting, uploadId);
            if (!FileUtil.exist(trailerVideo)) {
                log.error("(7)预告片文件不存在");
                return;
            }
            stopWatch.stop();
            log.debug("(7)结束{}, 视频位置:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), trailerVideo.getAbsolutePath(), stopWatch.getLastTaskTimeMillis());
            uploadTrailerQueue.put(UploadTrailerParameter.builder()
                    .uploadId(uploadId)
                    .trailerVideoPath(trailerVideo.getAbsolutePath())
                    .build());
        } catch (Exception e) {
            log.error("CutTrailerConsumer异常", e);
        }
    }
}
