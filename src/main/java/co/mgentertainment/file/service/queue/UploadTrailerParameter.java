package co.mgentertainment.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadTrailerParameter
 */
@Data
@Builder
public class UploadTrailerParameter implements Serializable {

    private static final long serialVersionUID = -9066373081550917343L;

    private Long uploadId;

    private String trailerVideoPath;

    private String watermarkVideoPath;
}