package co.mgentertainment.file.service.queue;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author larry
 * @createTime 2023/10/31
 * @description VideoWatermarkRequest
 */
@Data
@Builder
public class PrintWatermarkParameter implements Serializable {

    private static final long serialVersionUID = 6071987361599766039L;

    private String originVideoPath;

    private Long uploadId;
}
