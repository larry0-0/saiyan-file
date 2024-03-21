package co.saiyan.file.service.event;//package co.saiyan.file.service.event;

import co.saiyan.common.eventbus.AbstractEventSubscriber;
import co.saiyan.file.service.queue.PrintWatermarkParameter;
import co.saiyan.file.service.queue.PrintWatermarkQueue;
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
public class PrintWatermarkEventSubscriber extends AbstractEventSubscriber<PrintWatermarkEvent> {

    private final PrintWatermarkQueue printWatermarkQueue;

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void subscribe(PrintWatermarkEvent event) {
        Long uploadId = event.getUploadId();
        String originVideoPath = event.getOriginVideoPath();
        try {
            printWatermarkQueue.put(PrintWatermarkParameter.builder()
                    .uploadId(uploadId)
                    .originVideoPath(originVideoPath)
                    .build());
        } catch (Exception e) {
            log.error("打水印事件异常", e);
        }
    }
}
