package co.mgentertainment.file.service.dto;

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

    private Integer duration;

    private Integer startFromProportion;

}
