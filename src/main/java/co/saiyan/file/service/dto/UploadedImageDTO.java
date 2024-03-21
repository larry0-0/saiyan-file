package co.saiyan.file.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadImageDTO
 */
@Data
@Builder
@Schema(description = "已上传图片信息")
public class UploadedImageDTO {
    @Schema(description = "原始文件名")
    private String filename;
    @Schema(description = "图片相对路径")
    private String imagePath;
    @Schema(description = "缩略图相对路径")
    private String thumbnailPath;
}
