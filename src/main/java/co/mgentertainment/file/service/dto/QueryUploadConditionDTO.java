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
@Schema(description = "文件上传信息查询")
public class QueryUploadConditionDTO {
    @Schema(description = "页码")
    private Integer pageNo;
    @Schema(description = "每页数量")
    private Integer pageSize;
    @Schema(description = "应用编码")
    private String appCode;
    @Schema(description = "文件标题")
    private String title;
    @Schema(description = "文件类型")
    private Integer resourceType;
    @Schema(description = "主上传状态")
    private Integer status;
    @Schema(description = "子上传状态")
    private Integer subStatus;
    @Schema(description = "上传开始时间")
    private Date uploadStartDate;
    @Schema(description = "上传结束时间")
    private Date uploadEndDate;
}
