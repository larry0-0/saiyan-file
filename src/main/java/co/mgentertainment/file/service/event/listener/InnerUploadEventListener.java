package co.mgentertainment.file.service.event.listener;

import co.mgentertainment.common.devent.DistributedEventCallback;
import co.mgentertainment.common.devent.annonation.DistributedEventListener;
import co.mgentertainment.common.utils.cache.CacheStore;
import co.mgentertainment.file.service.UploadWorkflowService;
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

    private final UploadWorkflowService uploadWorkflowService;

    private CacheStore localCache = new CacheStore.Builder().setCapacity(10).setExpireSec(3600).build();

    @Override
    public void onComplete(String dirPath) {
        Object obj = localCache.get(dirPath);
        if ("locker".equals(obj)) {
            return;
        }
        localCache.put(dirPath, "locker");
        uploadWorkflowService.startUploadingWithInnerDir(new File(dirPath));
        localCache.remove(dirPath);
    }
}
