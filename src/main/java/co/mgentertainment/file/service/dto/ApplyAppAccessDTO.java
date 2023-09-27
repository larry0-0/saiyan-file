package co.mgentertainment.file.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author larry
 * @createTime 2023/9/22
 * @description ApplyAppAccessDTO
 */
@Data
@Schema(description = "APP客户端申请接入参数")
public class ApplyAppAccessDTO {
    @Schema(description = "来源应用名 如configcenter，cxc等")
    @NotEmpty(message = "来源应用名不能为空")
    private String appName;
    @Schema(description = "加密算法类型")
    private String algorithmType;
}
