package co.mgentertainment.file.service;

import co.mgentertainment.common.model.media.UploadSubStatusEnum;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.config.CuttingSetting;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadWorkflowService
 */
public interface UploadWorkflowService {

    File printWatermark(File originVideo, Long uploadId);

    File convertVideo(File originVideo, Long uploadId);

    void captureAndUploadScreenshot(File originVideo, String subDirName, Long rid, Long uploadId);

    Long uploadFilmFolder2CloudStorage(File filmFolder, String subDirName, File originVideo, String appCode, Long uploadId);

    File cutVideo(File watermarkVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId);

    void uploadVideo2CloudStorage(File video, VideoType type, UploadSubStatusEnum nextStatus, String subDirName, Long rid, Long uploadId);

}
