package co.saiyan.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CaptureAndUploadCoverParameter
 */
@Data
@Builder
public class CaptureAndUploadCoverParameter implements Serializable {

    private static final long serialVersionUID = 929570694840297449L;

    private Long uploadId;

    private String originVideoPath;
}
