package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEvent;
import lombok.Builder;
import lombok.Data;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description VideoConvertEvent
 */
@Data
@Builder
public class VideoUploadEvent extends AbstractEvent {

    private File videoFolder;

    private Long uploadId;
}
