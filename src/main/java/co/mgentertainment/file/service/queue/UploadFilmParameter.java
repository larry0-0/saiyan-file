package co.mgentertainment.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadFilmParameter
 */
@Data
@Builder
public class UploadFilmParameter implements Serializable {

    private static final long serialVersionUID = 8331896762725075463L;

    private Long uploadId;

    private String originVideoPath;

    private String processedVideoPath;

    private String appCode;
}
