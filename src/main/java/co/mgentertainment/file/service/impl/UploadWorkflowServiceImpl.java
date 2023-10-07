package co.mgentertainment.file.service.impl;

import co.mgentertainment.common.model.media.ResourceTypeEnum;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.ResourcePathType;
import co.mgentertainment.file.service.config.VideoType;
import co.mgentertainment.file.service.dto.ResourceDTO;
import co.mgentertainment.file.service.exception.MediaConvertException;
import co.mgentertainment.file.service.exception.MediaCutException;
import co.mgentertainment.file.service.exception.UploadFilm2CloudException;
import co.mgentertainment.file.service.exception.UploadSingleVideo2CloudException;
import co.mgentertainment.file.service.utils.MediaHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadWorkflowService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadWorkflowServiceImpl implements UploadWorkflowService {

    private final FileService fileService;

    private final FfmpegService ffmpegService;

    private final CachedUidGenerator cachedUidGenerator;

    @Override
    @Retryable(value = {MediaConvertException.class}, maxAttempts = 1, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public File convertVideo(File originVideo, Long uploadId) {
        try {
            File filmFile = ffmpegService.mediaConvert(originVideo);
            fileService.updateUploadStatus(uploadId, UploadStatusEnum.UPLOADING);
            return filmFile;
        } catch (Throwable t) {
            throw new MediaConvertException("视频转码失败", t);
        }
    }

    @Override
    @Retryable(value = {UploadFilm2CloudException.class}, maxAttempts = 1, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    public Long uploadFilmFolder2CloudStorage(File filmFolder, String subDirName, File originVideo, String appCode, boolean isLastStep, Long uploadId) {
        if (filmFolder == null || !filmFolder.exists() || filmFolder.isFile()) {
            return null;
        }
        try {
            long rid = cachedUidGenerator.getUID();
            fileService.files2CloudStorage(filmFolder.listFiles(), ResourceTypeEnum.VIDEO, subDirName, rid);
            fileService.saveResource(ResourceDTO.builder()
                    .rid(rid)
                    .filename(originVideo.getName())
                    .type(Integer.valueOf(ResourceTypeEnum.VIDEO.getValue()).shortValue())
                    .folder(subDirName)
                    .size(MediaHelper.getMediaSize(originVideo.length()))
                    .appCode(appCode)
                    .duration(ffmpegService.getMediaDuration(originVideo))
                    .build());
            log.debug("正片上传成功后file_upload表填充rid, uploadId:{}, rid:{}", uploadId, rid);
            fileService.updateUploadRid(uploadId, isLastStep ? UploadStatusEnum.COMPLETED : UploadStatusEnum.TRAILER_CUTTING_AND_UPLOADING, rid);
            return rid;
        } catch (Throwable t) {
            throw new UploadFilm2CloudException("上传正片到云存储且更新数据表失败", t);
        }
    }

    @Override
    @Retryable(value = {MediaCutException.class}, maxAttempts = 1, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public File cutVideo(File originVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId) {
        try {
            File trailerVideo = ffmpegService.mediaCut(originVideo, type, cuttingSetting);
            fileService.updateUploadStatus(uploadId, type == VideoType.TRAILER ?
                    UploadStatusEnum.TRAILER_CUTTING_AND_UPLOADING :
                    type == VideoType.SHORT_VIDEO ? UploadStatusEnum.SHORT_VIDEO_CUTTING_AND_UPLOADING : null);
            return trailerVideo;
        } catch (Throwable t) {
            throw new MediaCutException("剪切" + (type == VideoType.TRAILER ? "预告片失败" : "短视频失败"), t);
        }
    }

    @Override
    @Retryable(value = {UploadSingleVideo2CloudException.class}, maxAttempts = 2, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    public void uploadVideo2CloudStorage(File video, VideoType type, boolean isLastStep, Long rid, String subDirName, Long uploadId) {
        try {
            ResourcePathType pathType = type == VideoType.TRAILER ? ResourcePathType.TRAILER : type == VideoType.SHORT_VIDEO ? ResourcePathType.SHORT : null;
            fileService.uploadLocalFile2Cloud(video, ResourceTypeEnum.VIDEO, pathType, rid, subDirName);
            fileService.updateUploadStatus(uploadId, isLastStep ? UploadStatusEnum.COMPLETED : UploadStatusEnum.SHORT_VIDEO_CUTTING_AND_UPLOADING);
            if (isLastStep) {
                MediaHelper.deleteCompletedVideoFolder(uploadId);
            }
        } catch (Throwable t) {
            throw new UploadSingleVideo2CloudException("上传" + (type == VideoType.TRAILER ? "预告片" : "短视频") + "失败", t);
        }
    }

    @Recover
    public File doMediaConvertRecover(MediaConvertException e, File originVideo, Long uploadId) {
        log.error("重试后视频转码仍然失败, uploadId:{}, filePath:{}", uploadId, originVideo.getAbsolutePath());
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.CONVERT_FAILURE);
        return null;
    }

    @Recover
    public Long doFilmUploadRecover(UploadFilm2CloudException e, File filmFolder, String subDirName, File originVideo, String appCode, boolean isLastStep, Long uploadId) {
        log.error("重试后上传正片到云存储且更新数据表仍然失败, uploadId:{}, folderPath:{}", uploadId, filmFolder.getAbsolutePath());
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.UPLOAD_FAILURE);
        return null;
    }

    @Recover
    public File doMediaCutRecover(MediaCutException e, File originVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId) {
        log.error("重试后{}剪切仍然失败, uploadId:{}, filePath:{}", type == VideoType.TRAILER ? "预告片" : "短视频", uploadId, originVideo.getAbsolutePath());
        fileService.updateUploadStatus(uploadId, type == VideoType.TRAILER ? UploadStatusEnum.TRAILER_CUT_FAILURE : UploadStatusEnum.SHORT_VIDEO_FAILURE);
        return null;
    }

    @Recover
    public void doSingleVideoUploadRecover(UploadSingleVideo2CloudException e, File video, VideoType type, boolean isLastStep, Long rid, String subDirName, Long uploadId) {
        log.error("重试后{}上传仍然失败, uploadId:{}, filePath:{}", type == VideoType.TRAILER ? "预告片" : "短视频", uploadId, video.getAbsolutePath());
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.TRAILER_UPLOAD_FAILURE);
    }
}