package co.mgentertainment.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadShortVideoParameter
 */
@Data
@Builder
public class UploadShortVideoParameter implements Serializable {

    private static final long serialVersionUID = 5251990104009577572L;

    private Long uploadId;

    private String shortVideoPath;

    private Long rid;

    private String cloudSubDirName;

}
