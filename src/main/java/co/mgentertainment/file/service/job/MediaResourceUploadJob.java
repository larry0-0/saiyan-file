package co.mgentertainment.file.service.job;

import co.mgentertainment.common.model.media.MgfsPath;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.queue.ConvertVideoParameter;
import co.mgentertainment.file.service.queue.ConvertVideoQueue;
import co.mgentertainment.file.service.utils.MediaHelper;
import co.mgentertainment.file.web.cache.ClientHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description 媒资库上传job
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MediaResourceUploadJob {

    private final FileService fileService;
    private final ConvertVideoQueue convertVideoQueue;

    /**
     * 每分钟检查一次需要执行中的视频
     */
    @Scheduled(fixedRate = 60 * 1000, initialDelay = 50000)
    @SchedulerLock(name = "MediaResourceUploadJob.uploadFromLocalPath", lockAtLeastFor = "30s", lockAtMostFor = "60s")
    public void uploadFromLocalPath() {
        File mediaResourceRootDir = new File(MgfsPath.MZK_PATH);
        if (!mediaResourceRootDir.exists()) {
            log.warn("媒资库根目录不存在");
            return;
        }
        File[] files = mediaResourceRootDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            // 过滤文件名非法字符
            String filename = MediaHelper.filterInvalidFilenameChars(file.getName());
            Long uploadId = fileService.addUploadVideoRecord(filename, CuttingSetting.builder()
                    .trailerDuration(30)
                    .trailerStartFromProportion(0)
                    .autoCaptureCover(true)
                    .build());
            File newOriginFile = MediaHelper.moveFileToUploadDir(file, uploadId, MgfsPath.MgfsPathType.MAIN);
            convertVideoQueue.put(ConvertVideoParameter.builder()
                    .uploadId(uploadId)
                    .originVideoPath(newOriginFile.getAbsolutePath())
                    .appCode(ClientHolder.getCurrentClient())
                    .build());
        }
    }
}
