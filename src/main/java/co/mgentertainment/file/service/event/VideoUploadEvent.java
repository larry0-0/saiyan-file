package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEvent;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.config.CuttingSetting;
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

    private File watermarkVideo;

    private String subDirName;

    private CuttingSetting cuttingSetting;

    private VideoType videoType;

    private Long rid;

    private String appCode;
}
