package co.mgentertainment.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CutTrailerParameter
 */
@Data
@Builder
public class CutTrailerParameter implements Serializable {

    private static final long serialVersionUID = -8882749214231528778L;

    private Long uploadId;

    private String watermarkVideoPath;

    private Integer trailerDuration;

    private Integer trailerStartFromProportion;

    private Long rid;
}
