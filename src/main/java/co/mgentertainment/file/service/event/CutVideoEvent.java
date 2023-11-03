package co.mgentertainment.file.service.event;//package co.mgentertainment.file.service.event;

import co.mgentertainment.common.eventbus.AbstractEvent;
import co.mgentertainment.common.model.media.VideoType;
import lombok.Builder;
import lombok.Data;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CutVideoEvent
 */
@Data
@Builder
public class CutVideoEvent extends AbstractEvent {

    private Long uploadId;

    private String watermarkVideoPath;

    private VideoType type;

    private Integer cutDuration;

    private Integer cutStartPos;
}
