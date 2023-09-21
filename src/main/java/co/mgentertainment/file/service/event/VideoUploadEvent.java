package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEvent;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.VideoType;
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

    private Long uploadId;

    private File originVideo;

    private File processedVideo;

    private CuttingSetting cuttingSetting;

    private VideoType videoType;

    private Long rid;

    private String appName;
}
