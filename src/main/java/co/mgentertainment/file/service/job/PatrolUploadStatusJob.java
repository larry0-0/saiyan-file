package co.mgentertainment.file.service.job;

import cn.hutool.core.collection.ListUtil;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.model.media.UploadSubStatusEnum;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private final UploadWorkflowService uploadWorkflowService;

    /**
     * 每30分钟检查一次执行中的file upload，全节点执行
     */
    @Scheduled(fixedRate = 1800 * 1000, initialDelay = 30000)
    public void checkUploadStatus() {
        // 获取执行超过60分钟的未完成记录
        List<FileUploadDO> uploads = fileUploadRepository.getUploadsByStatusInTime(
                ListUtil.of(
                        UploadStatusEnum.CONVERT_FAILURE,
                        UploadStatusEnum.UPLOAD_FAILURE,
                        UploadStatusEnum.CAPTURE_FAILURE,
                        UploadStatusEnum.UPLOAD_COVER_FAILURE),
                ListUtil.of(
                        UploadSubStatusEnum.PRINT_FAILURE,
                        UploadSubStatusEnum.UPLOAD_ORIGIN_FAILURE,
                        UploadSubStatusEnum.CUT_TRAILER_FAILURE,
                        UploadSubStatusEnum.UPLOAD_TRAILER_FAILURE,
                        UploadSubStatusEnum.CUT_SHORT_FAILURE,
                        UploadSubStatusEnum.UPLOAD_SHORT_FAILURE),
                DateUtils.addMinutes(new Date(), -60));
        if (CollectionUtils.isEmpty(uploads)) {
            return;
        }

        uploads.stream().filter(fu -> fu != null && StringUtils.isNotEmpty(fu.getFilename())).forEach(fu -> {
            boolean hasTrailer = Optional.ofNullable(fu.getHasTrailer()).orElse((byte) 0).intValue() == 1;
            boolean hasShort = Optional.ofNullable(fu.getHasShort()).orElse((byte) 0).intValue() == 1;
            boolean hasCover = Optional.ofNullable(fu.getHasCover()).orElse((byte) 0).intValue() == 1;
            uploadWorkflowService.recoverUploading(fu.getUploadId(),
                    CuttingSetting.builder()
                            .trailerDuration(hasTrailer ? fu.getTrailerDuration() : null)
                            .trailerStartFromProportion(hasTrailer ? fu.getTrailerStartPos() : null)
                            .shortVideoDuration(hasShort ? fu.getShortDuration() : null)
                            .shortVideoStartFromProportion(hasShort ? fu.getShortStartPos() : null)
                            .autoCaptureCover(hasCover)
                            .build());
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }
        });
    }
}
