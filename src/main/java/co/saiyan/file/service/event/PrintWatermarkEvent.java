package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEvent;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description PrintWatermarkEvent
 */
@Data
@Builder
public class PrintWatermarkEvent extends AbstractEvent {

    private Long uploadId;

    private String originVideoPath;
}
