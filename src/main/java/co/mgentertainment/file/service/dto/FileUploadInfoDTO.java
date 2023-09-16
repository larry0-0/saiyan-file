package co.mgentertainment.file.service.dto;

import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description FileUploadInfoDTO
 */
@Data
@Builder
public class FileUploadInfoDTO {
    private String fileName;
    private String encryptResourceAddress;
    private Long uploadId;
    private UploadStatusEnum statusEnum;
}
