package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEvent;
import co.saiyan.common.model.media.VideoType;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description UploadVideoEvent
 */
@Data
@Builder
public class UploadSingleVideoEvent extends AbstractEvent {

    private Long uploadId;

    private VideoType type;

    private String videoPath;
}
