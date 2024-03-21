package co.saiyan.file.service.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description FileUploadDTO
 */
@Data
public class FileUploadDTO {

    @NotEmpty(message = "上传id不能为空")
    private Long uploadId;

    private String title;

    private String filename;
    @NotEmpty(message = "上传状态不能为空")
    private Short status;

    private Short subStatus;

    private String appCode;

    private Long rid;
}
