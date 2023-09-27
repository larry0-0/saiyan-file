package co.mgentertainment.file.service.config;

import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description CuttingSetting
 */
@Data
@Builder
public class CuttingSetting {

    private Integer duration;

    private Integer startFromProportion;

}
