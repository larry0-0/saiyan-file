package co.mgentertainment.file.service;

import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.config.CuttingSetting;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadWorkflowService
 */
public interface UploadWorkflowService {

    void printWatermark(File originVideo, Long uploadId);

    void convertVideo(File originVideo, Long uploadId);

    void captureAndUploadScreenshot(File originVideo, Long uploadId);

    void uploadFilmFolder2CloudStorage(File filmFolder, File originVideo, String appCode, Long uploadId);

    void cutVideo(File watermarkVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId);

    void uploadVideo2CloudStorage(File video, VideoType type, Long uploadId);

}
