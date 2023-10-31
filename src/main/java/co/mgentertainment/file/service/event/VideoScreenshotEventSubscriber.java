//package co.mgentertainment.file.service.event;
//
//import co.mgentertainment.common.eventbus.AbstractEventSubscriber;
//import co.mgentertainment.common.model.media.MgfsPath;
//import co.mgentertainment.file.service.UploadWorkflowService;
//import co.mgentertainment.file.service.utils.MediaHelper;
//import com.google.common.eventbus.AllowConcurrentEvents;
//import com.google.common.eventbus.Subscribe;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//
///**
// * @author larry
// * @createTime 2023/9/16
// * @description VideoUploadEventSubscriber
// */
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class VideoScreenshotEventSubscriber extends AbstractEventSubscriber<VideoScreenshotEvent> {
//
//    private final UploadWorkflowService uploadWorkflowService;
//
//    @Override
//    @Subscribe
//    @AllowConcurrentEvents
//    public void subscribe(VideoScreenshotEvent event) {
//        try {
//            File originVideo = event.getOriginVideo();
//            uploadWorkflowService.captureAndUploadScreenshot(originVideo, event.getSubDirName(), event.getRid(), event.getUploadId());
//        } catch (Exception e) {
//            log.error("截取封面事件异常", e);
//        } finally {
//            MediaHelper.deleteCompletedVideoFolder(event.getUploadId(), MgfsPath.MgfsPathType.MAIN);
//        }
//    }
//}
