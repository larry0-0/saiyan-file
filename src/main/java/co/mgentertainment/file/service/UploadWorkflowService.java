package co.mgentertainment.file.service;

import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.VideoType;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadWorkflowService
 */
public interface UploadWorkflowService {

    File convertVideo(File originVideo, Long uploadId);

    Long uploadFilmFolder2CloudStorage(File filmFolder, String subDirName, File originVideo, String appCode, boolean isLastStep, Long uploadId);

    File cutVideo(File originVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId);

    void uploadVideo2CloudStorage(File video, VideoType type, boolean isLastStep, Long rid, String subDirName, Long uploadId);
}
