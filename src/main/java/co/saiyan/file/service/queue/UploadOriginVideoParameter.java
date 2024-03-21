package co.saiyan.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadOriginVideoParameter
 */
@Data
@Builder
public class UploadOriginVideoParameter implements Serializable {

    private static final long serialVersionUID = 1956761780562507256L;

    private Long uploadId;

    private String watermarkVideoPath;

}
