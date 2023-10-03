package co.mgentertainment.file.service.job;

import cn.hutool.core.collection.ListUtil;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void checkUploadStatus() {
        // 获取执行超过10分钟的未完成记录
        FileUploadExample example = new FileUploadExample();
        example.createCriteria()
                .andDeletedEqualTo((byte) 0)
                .andStatusIn(ListUtil.of(Integer.valueOf(UploadStatusEnum.CONVERT_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadStatusEnum.UPLOAD_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadStatusEnum.TRAILER_CUT_FAILURE.getValue()).shortValue(),
                        Integer.valueOf(UploadStatusEnum.TRAILER_UPLOAD_FAILURE.getValue()).shortValue()))
                .andCreateTimeLessThan(DateUtils.addMinutes(new Date(), -10));
        List<FileUploadDO> fileUploadDOS = fileUploadRepository.getFileUploadsByExample(example);
        if (CollectionUtils.isEmpty(fileUploadDOS)) {
            return;
        }
        fileUploadDOS.stream().filter(fu -> StringUtils.isNotEmpty(fu.getFilename())).forEach(fu -> {
            fileService.reuploadVideo(fu.getUploadId(), CuttingSetting.builder().startFromProportion(0).duration(30).build());
        });
    }
}
