package co.mgentertainment.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CutShortVideoParameter
 */
@Data
@Builder
public class CutShortVideoParameter implements Serializable {

    private static final long serialVersionUID = -4910162223212314301L;

    private Long uploadId;

    private String watermarkVideoPath;

    private Integer shortVideoDuration;

    private Integer shortVideoStartFromProportion;

    private Long rid;

}
