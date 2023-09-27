package co.mgentertainment.file.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description ResourceDTO
 */
@Data
@Builder
public class ResourceDTO {

    private Long rid;
    private String filename;
    private String folder;
    private BigDecimal size;
    private Short type;
    private Integer duration;
    private String appName;
}
