package co.mgentertainment.file.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description FileUploadInfoDTO
 */
@Data
@Builder
@Schema(description = "视频上传信息")
public class VideoUploadInfoDTO {
    @Schema(description = "上传id")
    private Long uploadId;
    @Schema(description = "原始文件名")
    private String filename;
    @Schema(description = "正片相对路径 仅状态已完成时有值")
    private String filmPath;
    @Schema(description = "预告片相对路径 仅状态已完成时有值")
    private String trailerPath;
    @Schema(description = "上传状态")
    private String status;
}
