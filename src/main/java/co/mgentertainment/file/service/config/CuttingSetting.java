package co.mgentertainment.file.service.config;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description CuttingSetting
 */
@Data
@Builder
public class CuttingSetting implements Serializable {

    private static final long serialVersionUID = -2047530586604022418L;

    private Integer trailerDuration;

    private Integer trailerStartFromProportion;

    private Integer shortVideoDuration;

    private Integer shortVideoStartFromProportion;

    private Boolean autoCaptureCover;
}
