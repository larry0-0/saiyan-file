package co.saiyan.file.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author larry
 */
@Data
public class ResourceLineDTO {

    @Schema(description = "路线名称")
    private String lineName;

    @Schema(description = "路线地址")
    private String domainUrl;

    @Schema(description = "优先级")
    private Short priority;
}