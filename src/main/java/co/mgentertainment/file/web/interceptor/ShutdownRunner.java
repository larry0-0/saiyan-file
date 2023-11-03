package co.mgentertainment.file.web.interceptor;

import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.model.media.UploadSubStatusEnum;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * @author larry
 * @createTime 2023/11/3
 * @description EndApplicationWorker
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShutdownRunner implements CommandLineRunner {

    private final FileUploadRepository fileUploadRepository;

    @PreDestroy
    public void destory() {
        log.info("在程序关闭后执行");
        // main process
        updateStatus(UploadStatusEnum.CONVERTING, UploadStatusEnum.CONVERT_FAILURE);
        updateStatus(UploadStatusEnum.UPLOADING_FILM, UploadStatusEnum.UPLOAD_FAILURE);
        updateStatus(UploadStatusEnum.CAPTURING_AND_UPLOADING_COVER, UploadStatusEnum.CAPTURE_FAILURE);
        // vice process
        updateSubStatus(UploadSubStatusEnum.PRINTING, UploadSubStatusEnum.PRINT_FAILURE);
        updateSubStatus(UploadSubStatusEnum.UPLOADING_ORIGIN, UploadSubStatusEnum.UPLOAD_ORIGIN_FAILURE);
        updateSubStatus(UploadSubStatusEnum.CUTTING_TRAILER, UploadSubStatusEnum.CUT_TRAILER_FAILURE);
        updateSubStatus(UploadSubStatusEnum.UPLOADING_TRAILER, UploadSubStatusEnum.UPLOAD_TRAILER_FAILURE);
        updateSubStatus(UploadSubStatusEnum.CUTTING_SHORT, UploadSubStatusEnum.CUT_SHORT_FAILURE);
        updateSubStatus(UploadSubStatusEnum.UPLOADING_SHORT, UploadSubStatusEnum.UPLOAD_SHORT_FAILURE);
    }

    private void updateStatus(UploadStatusEnum replacedStatus, UploadStatusEnum replacingStatus) {
        fileUploadRepository.updateFileUpload(new FileUploadDO() {{
            setStatus(replacingStatus.getValue().shortValue());
        }}, new FileUploadExample() {{
            createCriteria().andCreateTimeGreaterThan(DateUtils.addMinutes(new Date(), -60))
                    .andDeletedEqualTo((byte) 0)
                    .andStatusEqualTo(replacedStatus.getValue().shortValue());
        }});
    }

    private void updateSubStatus(UploadSubStatusEnum replacedSubStatus, UploadSubStatusEnum replacingSubStatus) {
        fileUploadRepository.updateFileUpload(new FileUploadDO() {{
            setSubStatus(replacingSubStatus.getValue().shortValue());
        }}, new FileUploadExample() {{
            createCriteria().andCreateTimeGreaterThan(DateUtils.addMinutes(new Date(), -60))
                    .andDeletedEqualTo((byte) 0)
                    .andSubStatusEqualTo(replacedSubStatus.getValue().shortValue());
        }});
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
