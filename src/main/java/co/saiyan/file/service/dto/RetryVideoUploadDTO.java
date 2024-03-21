package co.saiyan.file.service.dto;

import co.saiyan.file.service.config.CuttingSetting;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description RetryVideoUploadDTO
 */
@Data
@Builder
public class RetryVideoUploadDTO {

    private Long uploadId;

    private CuttingSetting cuttingSetting;
}
