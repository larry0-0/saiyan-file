package co.mgentertainment.file.service.queue;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.common.utils.queue.AbstractDisruptorWorkConsumer;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

/**
 * @author larry
 * @createTime 2023/9/24
 * @description UploadFilmConsumer
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UploadFilmConsumer extends AbstractDisruptorWorkConsumer<UploadFilmParameter> {

    private final UploadWorkflowService uploadWorkflowService;

    private final FileService fileService;

    private final CaptureAndUploadCoverQueue captureAndUploadCoverQueue;

    @Override
    public void consume(UploadFilmParameter parameter) {
        Long uploadId = parameter.getUploadId();
        if (uploadId == null) {
            log.error("UploadFilmConsumer参数异常");
            return;
        }
        String originVideoPath = parameter.getOriginVideoPath();
        String processedVideoPath = parameter.getProcessedVideoPath();
        String appCode = parameter.getAppCode();
        try {
            File originVideo = FileUtil.exist(originVideoPath) ? new File(originVideoPath) : fileService.getMainOriginFile(uploadId);
            File folderToUpload = FileUtil.exist(originVideoPath) ? new File(processedVideoPath).getParentFile() : fileService.getConvertedFilmDir(uploadId);
            if (!FileUtil.exist(folderToUpload) || folderToUpload.isFile()) {
                log.error("待上传的视频文件夹{}不存在", folderToUpload.getAbsolutePath());
                return;
            }
            String subDirName = DateUtils.format(new Date(), DateUtils.FORMAT_YYYYMMDD);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("上传正片并添加资源记录");
            log.debug("(3)开始{}, uploadId:{}, 正片目录:{}", stopWatch.currentTaskName(), uploadId, folderToUpload.getAbsolutePath());
            Long rid = uploadWorkflowService.uploadFilmFolder2CloudStorage(folderToUpload, subDirName, originVideo, appCode, uploadId);
            stopWatch.stop();
            log.debug("(3)结束{}, uploadId:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, stopWatch.getLastTaskTimeMillis());
            if (rid == null) {
                log.error("(3)rid返回为空");
                return;
            }
            captureAndUploadCoverQueue.put(CaptureAndUploadCoverParameter.builder()
                    .uploadId(uploadId)
                    .originVideoPath(originVideoPath)
                    .build());
        } catch (Exception e) {
            log.error("UploadFilmConsumer异常", e);
        }
    }
}
