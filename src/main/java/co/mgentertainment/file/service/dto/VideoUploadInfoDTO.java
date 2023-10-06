package co.mgentertainment.file.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

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
    @Schema(description = "视频大小")
    private String size;
    @Schema(description = "视频时长")
    private String duration;
    @Schema(description = "正片相对路径 仅状态已完成时有值")
    private String filmPath;
    @Schema(description = "预告片相对路径 仅状态已完成时有值")
    private String trailerPath;
    @Schema(description = "短视频相对路径 仅状态已完成时有值")
    private String shortPath;
    @Schema(description = "上传状态")
    private String status;
    @Schema(description = "上传状态码")
    private Integer statusCode;
    @Schema(description = "上传开始时间")
    private Date uploadStartTime;
    @Schema(description = "状态更新时间")
    private Date statusUpdateTime;
}
