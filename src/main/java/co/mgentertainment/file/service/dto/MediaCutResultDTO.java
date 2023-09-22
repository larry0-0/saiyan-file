package co.mgentertainment.file.service.dto;

import lombok.Builder;
import lombok.Data;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/22
 * @description MediaCutDTO
 */
@Data
@Builder
public class MediaCutResultDTO {

    /**
     * 预告片
     */
    private File trailerFile;

    /**
     * 正片时长
     */
    private Integer filmDuration;

}
