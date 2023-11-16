package co.mgentertainment.file.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description VideoUploadInfoDTO
 */
@Data
@Builder
@Schema(description = "视频上传信息")
public class VideoUploadInfoDTO {
    @Schema(description = "上传id")
    private Long uploadId;
    @Schema(description = "文件名")
    private String filename;
    @Schema(description = "文件标题")
    private String title;
    @Schema(description = "视频大小")
    private String size;
    @Schema(description = "视频时长")
    private Integer duration;
    @Schema(description = "视频时长格式字符串")
    private String durationStr;
    @Schema(description = "原片(可设水印)相对路径 仅状态已完成时有值")
    private String originPath;
    @Schema(description = "转码片(可设水印)相对路径 仅状态已完成时有值")
    private String filmPath;
    @Schema(description = "预告片(可设水印)相对路径 仅状态已完成时有值")
    private String trailerPath;
    @Schema(description = "短视频(可设水印)相对路径 仅状态已完成时有值")
    private String shortPath;
    @Schema(description = "截图封面相对路径 仅状态已完成时有值")
    private String screenshotPath;
    @Schema(description = "截图缩略图相对路径 仅状态已完成时有值")
    private String screenshotThumbnailPath;
    @Schema(description = "主流程上传状态")
    private String status;
    @Schema(description = "主流程上传状态码")
    private Integer statusCode;
    @Schema(description = "子流程上传状态")
    private String subStatus;
    @Schema(description = "子流程上传状态码")
    private Integer subStatusCode;
    @Schema(description = "上传开始时间")
    private Date uploadStartTime;
    @Schema(description = "状态更新时间")
    private Date statusUpdateTime;
}
