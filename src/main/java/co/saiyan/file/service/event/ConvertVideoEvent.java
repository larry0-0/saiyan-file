package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEvent;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CutVideoEvent
 */
@Data
@Builder
public class ConvertVideoEvent extends AbstractEvent {

    private Long uploadId;

    private String originVideoPath;

    private Boolean isShortVideo;
}
