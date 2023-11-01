package co.mgentertainment.file.service.event.listener;

import co.mgentertainment.common.devent.DistributedEventCallback;
import co.mgentertainment.common.devent.annonation.DistributedEventListener;
import co.mgentertainment.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/10/27
 * @description InnerUploadEventListener
 */
@DistributedEventListener(eventKey = DistributedEventKey.UPLOADS)
@Component
@RequiredArgsConstructor
@Slf4j
public class InnerUploadEventListener implements DistributedEventCallback {

    private final FileService fileService;

    @Override
    public void onComplete(String dirPath) {
        fileService.startInnerUploads(new File(dirPath));
    }
}
