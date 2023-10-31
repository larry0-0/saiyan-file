package co.mgentertainment.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description ConvertVideoParameter
 */
@Data
@Builder
public class ConvertVideoParameter implements Serializable {

    private static final long serialVersionUID = -1988156382901064830L;

    private Long uploadId;

    private String originVideoPath;

    private String appCode;
}
