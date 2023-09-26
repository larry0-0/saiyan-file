package co.mgentertainment.file.service;

import co.mgentertainment.file.service.config.CuttingSetting;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadWorkflowService
 */
public interface UploadWorkflowService {

    File convertVideo(File originVideo, Long uploadId);

    Long uploadFilmFolder2CloudStorage(File filmFolder, String subDirName, File originVideo, String appCode, Long uploadId);

    File cutVideo(File originVideo, CuttingSetting cuttingSetting, Long uploadId);

    void uploadTrailer2CloudStorage(File trailerVideo, Long rid, String subDirName, Long uploadId);
}
