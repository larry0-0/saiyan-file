package co.mgentertainment.file.service;

import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.dto.VideoUploadInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadWorkflowService
 */
public interface UploadWorkflowService {

    VideoUploadInfoDTO startUploadingWithMultipartFile(MultipartFile multipartFile, CuttingSetting cuttingSetting, Boolean isShortVideo);

    void startUploadingWithInnerDir(File innerDirToUpload);

    void recoverUploading(Long uploadId, CuttingSetting cuttingSetting);

    void printWatermark(File originVideo, Long uploadId);

    void convertVideo(File originVideo, Long uploadId);

    void captureAndUploadScreenshot(File originVideo, Long uploadId);

    void uploadFilmFolder2CloudStorage(File filmFolder, File originVideo, String appCode, Long uploadId);

    void cutVideo(File watermarkVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId);

    void uploadVideo2CloudStorage(File video, VideoType type, Long uploadId);

    void afterMainProcessComplete(Long uploadId, File originVideo);

    void afterViceProcessComplete(Long uploadId);
}
