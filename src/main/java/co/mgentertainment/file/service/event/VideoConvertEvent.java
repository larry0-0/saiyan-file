package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEvent;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description VideoConvertEvent
 */
@Data
@Builder
public class VideoConvertEvent extends AbstractEvent {

    private String videoFilePath;

    private Long uploadId;
}
