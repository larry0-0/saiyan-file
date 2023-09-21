package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEvent;
import co.mgentertainment.file.service.config.CuttingSetting;
import lombok.Builder;
import lombok.Data;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description VideoCutEvent
 */
@Data
@Builder
public class VideoCutEvent extends AbstractEvent {

    private Long uploadId;

    private File originVideo;

    private CuttingSetting cuttingSetting;

}
