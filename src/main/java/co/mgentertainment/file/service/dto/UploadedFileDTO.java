package co.mgentertainment.file.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadedFileDTO
 */
@Data
@Builder
@Schema(description = "已上传文件信息")
public class UploadedFileDTO {
    @Schema(description = "原始文件名")
    private String filename;
    @Schema(description = "文件相对路径")
    private String remotePath;
}
