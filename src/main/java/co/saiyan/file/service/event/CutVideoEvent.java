package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEvent;
import co.saiyan.common.model.media.VideoType;
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
