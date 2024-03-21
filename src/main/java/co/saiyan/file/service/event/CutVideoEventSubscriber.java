package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEventSubscriber;
import co.saiyan.common.model.media.VideoType;
import co.saiyan.file.service.queue.*;
import co.saiyan.file.service.queue.CutShortVideoParameter;
import co.saiyan.file.service.queue.CutShortVideoQueue;
import co.saiyan.file.service.queue.CutTrailerParameter;
import co.saiyan.file.service.queue.CutTrailerQueue;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description CutVideoEventSubscriber
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CutVideoEventSubscriber extends AbstractEventSubscriber<CutVideoEvent> {

    private final CutTrailerQueue cutTrailerQueue;
    private final CutShortVideoQueue cutShortVideoQueue;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(CutVideoEvent event) {
        Long uploadId = event.getUploadId();
        String watermarkVideoPath = event.getWatermarkVideoPath();
        Integer cutDuration = event.getCutDuration();
        Integer cutStartPos = event.getCutStartPos();
        VideoType type = event.getType();
        try {
            switch (type) {
                case TRAILER:
                    cutTrailerQueue.put(CutTrailerParameter.builder()
                            .uploadId(uploadId)
                            .watermarkVideoPath(watermarkVideoPath)
                            .trailerDuration(cutDuration)
                            .trailerStartFromProportion(cutStartPos)
                            .build()
                    );
                    break;
                case SHORT_VIDEO:
                    cutShortVideoQueue.put(CutShortVideoParameter.builder()
                            .uploadId(uploadId)
                            .watermarkVideoPath(watermarkVideoPath)
                            .shortVideoDuration(cutDuration)
                            .shortVideoStartFromProportion(cutStartPos)
                            .build()
                    );
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("剪切视频事件异常", e);
        }
    }
}
