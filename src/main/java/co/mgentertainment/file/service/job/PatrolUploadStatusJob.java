package co.mgentertainment.file.service.job;

import cn.hutool.core.collection.ListUtil;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.model.media.UploadSubStatusEnum;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description PatrolUploadStatusJob
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PatrolUploadStatusJob {

    private final FileUploadRepository fileUploadRepository;
    private final FileService fileService;

    /**
     * 每10分钟检查一次执行中的file upload
     */
    @Scheduled(fixedRate = 600 * 1000, initialDelay = 30000)
    @SchedulerLock(name = "PatrolUploadStatusJob.checkUploadStatus", lockAtLeastFor = "30s", lockAtMostFor = "60s")
    public void checkUploadStatus() {
        // 获取执行超过10分钟的未完成记录
        FileUploadExample example = new FileUploadExample();
        example.createCriteria()
                .andDeletedEqualTo((byte) 0)
                .andStatusIn(ListUtil.of(
                        Integer.valueOf(UploadStatusEnum.CONVERT_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadStatusEnum.UPLOAD_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadStatusEnum.CAPTURE_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadStatusEnum.UPLOAD_COVER_FAILURE.getValue()).shortValue()))
                .andCreateTimeLessThan(DateUtils.addMinutes(new Date(), -10));
        List<FileUploadDO> fileUploadDOS = fileUploadRepository.getFileUploadsByExample(example);
        if (CollectionUtils.isEmpty(fileUploadDOS)) {
            return;
        }
        FileUploadExample example2 = new FileUploadExample();
        example2.createCriteria()
                .andDeletedEqualTo((byte) 0)
                .andSubStatusIn(ListUtil.of(
                        Integer.valueOf(UploadSubStatusEnum.PRINT_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadSubStatusEnum.UPLOAD_ORIGIN_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadSubStatusEnum.CUT_TRAILER_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadSubStatusEnum.UPLOAD_TRAILER_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadSubStatusEnum.CUT_SHORT_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadSubStatusEnum.UPLOAD_SHORT_FAILURE.getValue()).shortValue()))
                .andCreateTimeLessThan(DateUtils.addMinutes(new Date(), -10));
        List<FileUploadDO> fileUploadDOList = fileUploadRepository.getFileUploadsByExample(example);
        fileUploadDOS.addAll(fileUploadDOList);
        fileUploadDOS.stream().filter(fu -> StringUtils.isNotEmpty(fu.getFilename())).forEach(fu -> {
            boolean hasTrailer = fu.getHasTrailer().equals(1);
            boolean hasShort = fu.getHasShort().equals(1);
            boolean hasCover = fu.getHasCover().equals(1);
            fileService.reuploadVideo(fu.getUploadId(),
                    CuttingSetting.builder()
                            .trailerDuration(hasTrailer ? fu.getTrailerDuration() : null)
                            .trailerStartFromProportion(hasTrailer ? fu.getTrailerStartPos() : null)
                            .shortVideoDuration(hasShort ? fu.getShortDuration() : null)
                            .shortVideoStartFromProportion(hasShort ? fu.getShortStartPos() : null)
                            .autoCaptureCover(hasCover)
                            .build());
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException ignored) {
            }
        });
    }
}
