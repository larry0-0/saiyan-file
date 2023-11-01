package co.mgentertainment.file.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.model.media.*;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.dto.ResourceDTO;
import co.mgentertainment.file.service.exception.*;
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
    @Retryable(value = {PrintWatermarkException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public File printWatermark(File originVideo, Long uploadId) {
        try {
            File filmFile = ffmpegService.printWatermark(originVideo);
            fileService.updateSubStatus(uploadId, UploadSubStatusEnum.UPLOADING_ORIGIN);
            return filmFile;
        } catch (Throwable t) {
            throw new PrintWatermarkException("视频打水印失败", t);
        }
    }

    @Override
    @Retryable(value = {MediaConvertException.class}, maxAttempts = 2, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public File convertVideo(File originVideo, Long uploadId) {
        try {
            File filmFile = ffmpegService.mediaConvert(originVideo, true, true);
            fileService.updateUploadStatus(uploadId, UploadStatusEnum.UPLOADING_FILM);
            return filmFile;
        } catch (Throwable t) {
            throw new MediaConvertException("视频转码失败", t);
        }
    }

    @Override
    @Retryable(value = {UploadFilm2CloudException.class}, maxAttempts = 2, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    public Long uploadFilmFolder2CloudStorage(File filmFolder, String subDirName, File originVideo, String appCode, Long uploadId) {
        if (!FileUtil.exist(filmFolder) || filmFolder.isFile()) {
            return null;
        }
        try {
            long rid = cachedUidGenerator.getUID();
            fileService.files2CloudStorage(filmFolder.listFiles(), ResourceTypeEnum.VIDEO, subDirName, rid);
            rid = fileService.saveResource(ResourceDTO.builder()
                    .rid(rid)
                    .filename(originVideo.getName())
                    .type(Integer.valueOf(ResourceTypeEnum.VIDEO.getValue()).shortValue())
                    .folder(subDirName)
                    .size(MediaHelper.getMediaSize(originVideo.length()))
                    .appCode(appCode)
                    .duration(ffmpegService.getMediaDuration(originVideo))
                    .build());
            fileService.updateUploadStatusAndRid(uploadId, UploadStatusEnum.CAPTURING_AND_UPLOADING_COVER, rid);
            log.debug("正片上传成功后file_upload表填充rid, film folder:{}, rid:{}", filmFolder.getAbsolutePath(), rid);
            return rid;
        } catch (Throwable t) {
            throw new UploadFilm2CloudException("上传正片到云存储且更新数据表失败", t);
        }
    }

    @Override
    @Retryable(value = {MediaCutException.class}, maxAttempts = 1, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public File cutVideo(File watermarkVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId) {
        try {
            File trailerVideo = ffmpegService.mediaCut(watermarkVideo, type, cuttingSetting);
            fileService.updateSubStatus(uploadId,
                    type == VideoType.TRAILER ? UploadSubStatusEnum.UPLOADING_TRAILER :
                            type == VideoType.SHORT_VIDEO ? UploadSubStatusEnum.UPLOADING_SHORT : null);
            return trailerVideo;
        } catch (Throwable t) {
            throw new MediaCutException("剪切" + (type == VideoType.TRAILER ? "预告片失败" : "短视频失败"), t);
        }
    }

    @Override
    @Retryable(value = {UploadSingleVideo2CloudException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    public void uploadVideo2CloudStorage(File video, VideoType type, UploadSubStatusEnum nextStatus, String subDirName, Long rid, Long uploadId) {
        try {
            ResourcePathType pathType = type == VideoType.ORIGIN_VIDEO ? ResourcePathType.ORIGIN :
                    type == VideoType.TRAILER ? ResourcePathType.TRAILER :
                            type == VideoType.SHORT_VIDEO ? ResourcePathType.SHORT : null;
            fileService.uploadLocalFile2Cloud(video, ResourceTypeEnum.VIDEO, subDirName, rid, pathType);
            fileService.updateSubStatus(uploadId, nextStatus);
        } catch (Throwable t) {
            throw new UploadSingleVideo2CloudException("上传" + (type == VideoType.TRAILER ? "预告片" : type == VideoType.ORIGIN_VIDEO ? "原片" : "短视频") + "失败", t);
        }
    }

    @Override
    @Retryable(value = {CaptureAndUploadScreenshotException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public void captureAndUploadScreenshot(File originVideo, String subDirName, Long rid, Long uploadId) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("剪切和上传封面");
            log.debug("(6.1)开始剪切封面, video:{}", originVideo.getAbsolutePath());
            File imgFile = ffmpegService.captureScreenshot(originVideo);
            log.debug("(6.2)开始上传封面, video:{}", originVideo.getAbsolutePath());
            fileService.uploadLocalFile2Cloud(imgFile, ResourceTypeEnum.VIDEO, subDirName, rid, ResourcePathType.COVER);
            stopWatch.stop();
            log.debug("(6.3){}流程结束，已完成. uploadId:{}, rid:{}, 耗时:{}毫秒", stopWatch.getLastTaskName(), uploadId, rid, stopWatch.getLastTaskTimeMillis());
        } catch (Throwable t) {
            throw new CaptureAndUploadScreenshotException("剪切和上传封面失败", t);
        }
    }

    @Recover
    public File doPrintWatermarkRecover(PrintWatermarkException e, File originVideo, Long uploadId) {
        log.error("重试后打水印仍然失败, filePath:{}", originVideo.getAbsolutePath(), e);
        fileService.updateSubStatus(uploadId, UploadSubStatusEnum.PRINT_FAILURE);
        return null;
    }

    @Recover
    public File doMediaConvertRecover(MediaConvertException e, File originVideo, Long uploadId) {
        try {
            File filmFile = ffmpegService.mediaConvert(originVideo, true, false);
            if (FileUtil.exist(filmFile)) {
                return filmFile;
            }
        } catch (Exception e1) {
            fileService.updateUploadStatus(uploadId, UploadStatusEnum.CONVERT_FAILURE);
        }
        log.error("重试后视频转码仍然失败, uploadId:{}, filePath:{}", uploadId, originVideo.getAbsolutePath(), e);
        return null;
    }

    @Recover
    public Long doFilmUploadRecover(UploadFilm2CloudException e, File filmFolder, String subDirName, File originVideo, String appCode, Long uploadId) {
        log.error("重试后上传正片到云存储且更新数据表仍然失败, uploadId:{}, folderPath:{}", uploadId, filmFolder.getAbsolutePath(), e);
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.UPLOAD_FAILURE);
        return null;
    }

    @Recover
    public File doMediaCutRecover(MediaCutException e, File watermarkVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId) {
        log.error("重试后{}剪切仍然失败, uploadId:{}, filePath:{}", type == VideoType.TRAILER ? "预告片" : "短视频", uploadId, watermarkVideo.getAbsolutePath(), e);
        fileService.updateSubStatus(uploadId, type == VideoType.TRAILER ? UploadSubStatusEnum.CUT_TRAILER_FAILURE : UploadSubStatusEnum.CUT_SHORT_FAILURE);
        return null;
    }

    @Recover
    public void doSingleVideoUploadRecover(UploadSingleVideo2CloudException e, File video, VideoType type, UploadSubStatusEnum nextStatus, String subDirName, Long rid, Long uploadId) {
        log.error("重试后{}上传仍然失败, uploadId:{}, filePath:{}", type == VideoType.ORIGIN_VIDEO ? "原片" : type == VideoType.TRAILER ? "预告片" : "短视频", uploadId, video.getAbsolutePath(), e);
        fileService.updateSubStatus(uploadId,
                type == VideoType.ORIGIN_VIDEO ? UploadSubStatusEnum.UPLOAD_ORIGIN_FAILURE :
                        type == VideoType.TRAILER ? UploadSubStatusEnum.UPLOAD_TRAILER_FAILURE :
                                type == VideoType.SHORT_VIDEO ? UploadSubStatusEnum.UPLOAD_SHORT_FAILURE : null);
    }

    @Recover
    public void doCaptureAndUploadScreenshotRecover(CaptureAndUploadScreenshotException e, File originVideo, String subDirName, Long rid, Long uploadId) {
        log.error("重试后截取和上传封面到云存储且更新数据表仍然失败, uploadId:{}, folderPath:{}", uploadId, originVideo.getAbsolutePath(), e);
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.CAPTURE_FAILURE);
    }
}