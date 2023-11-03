package co.mgentertainment.file.service.impl;

import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.model.R;
import co.mgentertainment.common.model.media.*;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.UploadWorkflowService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.dto.ResourceDTO;
import co.mgentertainment.file.service.dto.UploadResourceDTO;
import co.mgentertainment.file.service.dto.VideoUploadInfoDTO;
import co.mgentertainment.file.service.event.*;
import co.mgentertainment.file.service.exception.*;
import co.mgentertainment.file.service.utils.MediaHelper;
import co.mgentertainment.file.web.cache.ClientHolder;
import com.google.common.eventbus.AsyncEventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadWorkflowService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(10)
public class UploadWorkflowServiceImpl implements UploadWorkflowService {
    private final List<String> IGNORED_DIRS = Arrays.asList(".localized", ".DS_Store", "desktop.ini");

    private final AsyncEventBus eventBus;

    private final FileService fileService;

    private final FfmpegService ffmpegService;

    @Override
    public VideoUploadInfoDTO startUploadingWithMultipartFile(MultipartFile multipartFile, CuttingSetting cuttingSetting) {
        VideoUploadInfoDTO videoUploadInfo = fileService.uploadVideo(multipartFile, cuttingSetting);
        Long uploadId = videoUploadInfo.getUploadId();
        String filename = videoUploadInfo.getFilename();
        File file;
        try {
            file = saveMultipartFileInDisk(multipartFile, filename, uploadId);
        } catch (IOException e) {
            throw new RuntimeException("fail to persist file", e);
        }
        eventBus.post(ConvertVideoEvent.builder()
                .uploadId(uploadId)
                .originVideoPath(file.getAbsolutePath())
                .build()
        );
        return videoUploadInfo;
    }

    @Override
    public R<Void> startUploadingWithInnerDir(File innerDirToUpload) {
        if (!FileUtil.exist(innerDirToUpload) || FileUtil.isDirEmpty(innerDirToUpload)) {
            return R.failed("内部服务器目录不存在或没有文件");
        }
        File[] files = innerDirToUpload.listFiles();
        for (File file : files) {
            if (IGNORED_DIRS.contains(file.getName())) {
                continue;
            }
            // 过滤文件名非法字符
            String filename = MediaHelper.filterInvalidFilenameChars(file.getName());
            Long uploadId = fileService.addUploadVideoRecord(
                    filename,
                    CuttingSetting.builder()
                            .trailerDuration(30)
                            .trailerStartFromProportion(0)
                            .autoCaptureCover(true)
                            .build(),
                    Optional.of(FileService.SERVER_INNER_APP_CODE));
            File newOriginFile = MediaHelper.moveFileToUploadDir(file, uploadId, MgfsPath.MgfsPathType.MAIN);
            eventBus.post(ConvertVideoEvent.builder()
                    .uploadId(uploadId)
                    .originVideoPath(newOriginFile.getAbsolutePath())
                    .build());
        }
        return R.ok();
    }

    @Override
    public void recoverUploading(Long uploadId, CuttingSetting cuttingSetting) {
        FileUploadDO uploadRecord = fileService.getUploadRecord(uploadId);
        if (uploadRecord == null || uploadRecord.getUploadId() == null) {
            log.error("未找到uploadId:{}对应的上传记录", uploadId);
            return;
        }
        File originVideo = fileService.getOriginFile(uploadRecord);
        if (FileUtil.exist(originVideo)) {
            UploadStatusEnum oldStatus = UploadStatusEnum.getByValue(uploadRecord.getStatus().intValue());
            Long rid = uploadRecord.getRid();
            switch (oldStatus) {
                case CONVERT_FAILURE:
                    eventBus.post(ConvertVideoEvent.builder()
                            .uploadId(uploadId)
                            .originVideoPath(originVideo.getAbsolutePath())
                            .build());
                    break;
                case UPLOAD_FAILURE:
                    File filmVideo = MediaHelper.getProcessedFileByOriginFile(originVideo, VideoType.FEATURE_FILM.getValue(), ResourceSuffix.FEATURE_FILM);
                    eventBus.post(UploadFilmEvent.builder()
                            .uploadId(uploadId)
                            .originVideoPath(originVideo.getAbsolutePath())
                            .processedVideoPath(filmVideo.getAbsolutePath())
                            .appCode(ClientHolder.getCurrentClient())
                            .build());
                    break;
                case CAPTURE_FAILURE:
                case UPLOAD_COVER_FAILURE:
                    if (!fileService.existsRid(rid)) {
                        break;
                    }
                    eventBus.post(CaptureAndUploadCoverEvent.builder()
                            .uploadId(uploadId)
                            .originVideoPath(originVideo.getAbsolutePath())
                            .build());
                    break;
                default:
                    break;
            }
        }
        UploadSubStatusEnum oldSubStatus = UploadSubStatusEnum.getByValue(uploadRecord.getSubStatus().intValue());
        switch (oldSubStatus) {
            case PRINT_FAILURE:
                eventBus.post(PrintWatermarkEvent.builder()
                        .uploadId(uploadId)
                        .originVideoPath(originVideo.getAbsolutePath())
                        .build());
                break;
            case UPLOAD_ORIGIN_FAILURE:
                eventBus.post(UploadSingleVideoEvent.builder()
                        .uploadId(uploadId)
                        .videoPath(fileService.getWatermarkFile(uploadId).getAbsolutePath())
                        .type(VideoType.ORIGIN_VIDEO)
                        .build());
                break;
            case CUT_TRAILER_FAILURE:
                eventBus.post(CutVideoEvent.builder()
                        .uploadId(uploadId)
                        .watermarkVideoPath(fileService.getWatermarkFile(uploadId).getAbsolutePath())
                        .type(VideoType.TRAILER)
                        .cutDuration(Optional.ofNullable(cuttingSetting).orElse(CuttingSetting.builder().build()).getTrailerDuration())
                        .cutStartPos(Optional.ofNullable(cuttingSetting).orElse(CuttingSetting.builder().build()).getTrailerStartFromProportion())
                        .build());
                break;
            case UPLOAD_TRAILER_FAILURE:
                File trailer = fileService.getTrailerFile(uploadId);
                eventBus.post(UploadSingleVideoEvent.builder()
                        .uploadId(uploadId)
                        .videoPath(trailer.getAbsolutePath())
                        .type(VideoType.TRAILER)
                        .build());
                break;
            case CUT_SHORT_FAILURE:
                eventBus.post(CutVideoEvent.builder()
                        .uploadId(uploadId)
                        .watermarkVideoPath(fileService.getWatermarkFile(uploadId).getAbsolutePath())
                        .type(VideoType.SHORT_VIDEO)
                        .cutDuration(Optional.ofNullable(cuttingSetting).orElse(CuttingSetting.builder().build()).getShortVideoDuration())
                        .cutStartPos(Optional.ofNullable(cuttingSetting).orElse(CuttingSetting.builder().build()).getShortVideoStartFromProportion())
                        .build());
                break;
            case UPLOAD_SHORT_FAILURE:
                File shortVideo = fileService.getShortVideoFile(uploadId);
                eventBus.post(UploadSingleVideoEvent.builder()
                        .uploadId(uploadId)
                        .videoPath(shortVideo.getAbsolutePath())
                        .type(VideoType.SHORT_VIDEO)
                        .build());
                break;
            default:
                break;
        }
    }

    @Override
    @Retryable(value = {PrintWatermarkException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public void printWatermark(File originVideo, Long uploadId) {
        try {
            File originV = FileUtil.exist(originVideo) ? originVideo : fileService.getViceOriginFile(uploadId);
            File watermarkVideo = ffmpegService.printWatermark(originV);
            if (!FileUtil.exist(watermarkVideo)) {
                throw new PrintWatermarkException("视频打水印失败");
            }
            fileService.updateSubStatus(uploadId, UploadSubStatusEnum.UPLOADING_ORIGIN);
            eventBus.post(UploadSingleVideoEvent.builder()
                    .type(VideoType.ORIGIN_VIDEO)
                    .uploadId(uploadId)
                    .videoPath(Optional.ofNullable(watermarkVideo).orElse(originV).getAbsolutePath())
                    .build()
            );
        } catch (Throwable t) {
            throw new PrintWatermarkException("视频打水印失败", t);
        }
    }

    @Override
    @Retryable(value = {MediaConvertException.class}, maxAttempts = 2, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public void convertVideo(File originVideo, Long uploadId) {
        try {
            File originV = FileUtil.exist(originVideo) ? originVideo : fileService.getMainOriginFile(uploadId);
            File m3u8File = ffmpegService.mediaConvert(originV, true, true);
            fileService.updateUploadStatus(uploadId, UploadStatusEnum.UPLOADING_FILM);
            if (!FileUtil.exist(m3u8File)) {
                throw new MediaConvertException("视频转码失败");
            }
            eventBus.post(UploadFilmEvent.builder()
                    .uploadId(uploadId)
                    .originVideoPath(originV.getAbsolutePath())
                    .processedVideoPath(m3u8File.getAbsolutePath())
                    .appCode(ClientHolder.getCurrentClient())
                    .build()
            );
        } catch (Throwable t) {
            throw new MediaConvertException("视频转码失败", t);
        }
    }

    @Override
    @Retryable(value = {UploadFilm2CloudException.class}, maxAttempts = 2, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    public void uploadFilmFolder2CloudStorage(File filmFolder, File originVideo, String appCode, Long uploadId) {
        try {
            File folderToUpload = FileUtil.exist(filmFolder) ? filmFolder : fileService.getConvertedFilmDir(uploadId);
            if (!FileUtil.exist(folderToUpload) || folderToUpload.isFile()) {
                log.error("待上传的视频文件夹{}不存在", folderToUpload.getAbsolutePath());
                fileService.updateUploadStatus(uploadId, UploadStatusEnum.VIDEO_DAMAGED_OR_LOST);
                return;
            }
            File originV = FileUtil.exist(originVideo) ? originVideo : fileService.getMainOriginFile(uploadId);
            String subDirName = DateUtils.format(new Date(), DateUtils.FORMAT_YYYYMMDD);
            Long rid = fileService.saveResource(ResourceDTO.builder()
                    .filename(originV.getName())
                    .type(Integer.valueOf(ResourceTypeEnum.VIDEO.getValue()).shortValue())
                    .folder(subDirName)
                    .size(MediaHelper.getMediaSize(originV.length()))
                    .appCode(appCode)
                    .duration(ffmpegService.getMediaDuration(originV))
                    .build());
            fileService.files2CloudStorage(filmFolder.listFiles(), ResourceTypeEnum.VIDEO, subDirName, rid, true);
            if (!fileService.existsRid(rid)) {
                throw new UploadFilm2CloudException("rid未找到");
            }
            fileService.updateUploadStatusAndRid(uploadId, UploadStatusEnum.CAPTURING_AND_UPLOADING_COVER, rid);
            log.debug("正片上传成功后file_upload表填充rid, film folder:{}, rid:{}", filmFolder.getAbsolutePath(), rid);
            eventBus.post(CaptureAndUploadCoverEvent.builder()
                    .uploadId(uploadId)
                    .originVideoPath(originV.getAbsolutePath())
                    .build()
            );
        } catch (Throwable t) {
            throw new UploadFilm2CloudException("上传正片到云存储且更新数据表失败", t);
        }
    }

    @Override
    @Retryable(value = {MediaCutException.class}, maxAttempts = 1, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public void cutVideo(File watermarkVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId) {
        try {
            Integer trailerDuration = cuttingSetting.getTrailerDuration();
            Integer shortDuration = cuttingSetting.getShortVideoDuration();
            if (type == VideoType.TRAILER && trailerDuration == null) {
                UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
                if (uploadResource != null || uploadResource.getUploadId() == null) {
                    log.error("cut Trailer参数异常,未找到uploadId:{}", uploadId);
                    return;
                }
                cuttingSetting = CuttingSetting.builder().trailerDuration(uploadResource.getTrailerDuration()).trailerStartFromProportion(uploadResource.getTrailerStartPos()).build();
            } else if (type == VideoType.SHORT_VIDEO && shortDuration == null) {
                UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
                if (uploadResource != null || uploadResource.getUploadId() == null) {
                    log.error("cut Short参数异常,未找到uploadId:{}", uploadId);
                    return;
                }
                cuttingSetting = CuttingSetting.builder().shortVideoDuration(uploadResource.getShortDuration()).shortVideoStartFromProportion(uploadResource.getShortStartPos()).build();
            }
            File watermarkV = FileUtil.exist(watermarkVideo) ? watermarkVideo : fileService.getWatermarkFile(uploadId);

            File cutVideo = ffmpegService.mediaCut(watermarkV, type, cuttingSetting, true);
            if (!FileUtil.exist(cutVideo)) {
                throw new MediaCutException("剪切" + (type == VideoType.TRAILER ? "预告片失败" : "短视频失败"));
            }
            fileService.updateSubStatus(uploadId,
                    type == VideoType.TRAILER ? UploadSubStatusEnum.UPLOADING_TRAILER :
                            type == VideoType.SHORT_VIDEO ? UploadSubStatusEnum.UPLOADING_SHORT : null);
            eventBus.post(UploadSingleVideoEvent.builder()
                    .uploadId(uploadId)
                    .type(type)
                    .videoPath(cutVideo.getAbsolutePath())
                    .build());
        } catch (Throwable t) {
            throw new MediaCutException("剪切" + (type == VideoType.TRAILER ? "预告片失败" : "短视频失败"), t);
        }
    }

    @Override
    @Retryable(value = {UploadSingleVideo2CloudException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000L, multiplier = 1.5))
    public void uploadVideo2CloudStorage(File video, VideoType type, Long uploadId) {
        try {
            UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
            if (uploadResource == null || uploadResource.getRid() == null) {
                throw new UploadSingleVideo2CloudException("未找到uploadId:" + uploadId + "对应的rid");
            }
            Long rid = uploadResource.getRid();
            File uploadedVideo = FileUtil.exist(video) ? video :
                    type == VideoType.ORIGIN_VIDEO ? fileService.getWatermarkFile(uploadId) :
                            type == VideoType.TRAILER ? fileService.getTrailerFile(uploadId) :
                                    type == VideoType.SHORT_VIDEO ? fileService.getShortVideoFile(uploadId) :
                                            type == VideoType.FEATURE_FILM ? fileService.getViceOriginFile(uploadId) : null;
            ResourcePathType pathType = type == VideoType.ORIGIN_VIDEO ? ResourcePathType.ORIGIN :
                    type == VideoType.TRAILER ? ResourcePathType.TRAILER :
                            type == VideoType.SHORT_VIDEO ? ResourcePathType.SHORT : null;
            fileService.uploadLocalFile2Cloud(uploadedVideo, ResourceTypeEnum.VIDEO, uploadResource.getFolder(), rid, pathType);
            if (VideoType.FEATURE_FILM == type) {
                return;
            }
            boolean needTrailer = VideoType.ORIGIN_VIDEO == type && new Byte((byte) 1).equals(uploadResource.getHasTrailer());
            boolean needShort = Arrays.asList(VideoType.ORIGIN_VIDEO, VideoType.TRAILER).contains(type) && new Byte((byte) 1).equals(uploadResource.getHasShort());
            if (!needTrailer && !needShort) {
                afterViceProcessComplete(uploadId);
                return;
            }
            // 更新下一步的状态
            fileService.updateSubStatus(uploadId, needTrailer ? UploadSubStatusEnum.CUTTING_TRAILER : UploadSubStatusEnum.CUTTING_SHORT);
            eventBus.post(CutVideoEvent.builder()
                    .type(needTrailer ? VideoType.TRAILER : VideoType.SHORT_VIDEO)
                    .uploadId(uploadId)
                    .watermarkVideoPath(video.getAbsolutePath())
                    .cutDuration(needTrailer ? uploadResource.getTrailerDuration() : uploadResource.getShortDuration())
                    .cutStartPos(needTrailer ? uploadResource.getTrailerStartPos() : uploadResource.getShortStartPos())
                    .build()
            );
        } catch (Throwable t) {
            throw new UploadSingleVideo2CloudException("上传" + (type == VideoType.TRAILER ? "预告片" : type == VideoType.ORIGIN_VIDEO ? "原片" : "短视频") + "失败", t);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void afterMainProcessComplete(Long uploadId, File originVideo) {
        if (!FileUtil.exist(originVideo)) {
            originVideo = fileService.getMainOriginFile(uploadId);
        }
        // 移动原视频
        File newOriginFile = MediaHelper.moveFileToUploadDir(originVideo, uploadId, MgfsPath.MgfsPathType.VICE);
        // 删除已处理目录
        MediaHelper.deleteCompletedVideoFolder(uploadId, MgfsPath.MgfsPathType.MAIN);
        // 入水印处理队列
        eventBus.post(PrintWatermarkEvent.builder()
                .uploadId(uploadId)
                .originVideoPath(newOriginFile.getAbsolutePath())
                .build());
        // 更新状态
        fileService.updateStatus(uploadId, UploadStatusEnum.COMPLETED, UploadSubStatusEnum.PRINTING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void afterViceProcessComplete(Long uploadId) {
        // 删除已处理目录
        MediaHelper.deleteCompletedVideoFolder(uploadId, MgfsPath.MgfsPathType.VICE);
        // 更新状态
        fileService.updateSubStatus(uploadId, UploadSubStatusEnum.END);
    }

    @Override
    @Retryable(value = {CaptureAndUploadScreenshotException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1500L, multiplier = 1.5))
    public void captureAndUploadScreenshot(File originVideo, Long uploadId) {
        try {
            UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
            if (uploadResource == null || uploadResource.getUploadId() == null) {
                log.error("captureAndUploadScreenshot参数异常,未找到uploadId:{}", uploadId);
                return;
            }
            boolean hasCover = new Byte((byte) 1).equals(uploadResource.getHasCover());
            if (hasCover) {
                File originV = FileUtil.exist(originVideo) ? originVideo : fileService.getMainOriginFile(uploadId);
                File imgFile = ffmpegService.captureScreenshot(originV);
                Long rid = uploadResource.getRid();
                String subDirName = uploadResource.getFolder();
                fileService.uploadLocalFile2Cloud(imgFile, ResourceTypeEnum.VIDEO, subDirName, rid, ResourcePathType.COVER);
            } else {
                log.debug("无截取封面需求，直接跳过结束主流程");
            }
            afterMainProcessComplete(uploadId, originVideo);
        } catch (Throwable t) {
            throw new CaptureAndUploadScreenshotException("剪切和上传封面失败", t);
        }
    }

    @Recover
    public void doPrintWatermarkRecover(PrintWatermarkException e, File originVideo, Long uploadId) {
        log.error("重试后打水印仍然失败, filePath:{}", originVideo.getAbsolutePath(), e);
        fileService.updateSubStatus(uploadId, UploadSubStatusEnum.PRINT_FAILURE);
    }

    @Recover
    public void doMediaConvertRecover(MediaConvertException e, File originVideo, Long uploadId) {
        try {
            File filmFile = ffmpegService.mediaConvert(originVideo, true, false);
            if (FileUtil.exist(filmFile)) {
                return;
            }
        } catch (Exception ignored) {
        }
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.CONVERT_FAILURE);
        log.error("重试后视频转码仍然失败, uploadId:{}, filePath:{}", uploadId, originVideo.getAbsolutePath(), e);
    }

    @Recover
    public void doFilmUploadRecover(UploadFilm2CloudException e, File filmFolder, File originVideo, String appCode, Long uploadId) {
        log.error("重试后上传正片到云存储且更新数据表仍然失败, uploadId:{}, folderPath:{}", uploadId, filmFolder.getAbsolutePath(), e);
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.UPLOAD_FAILURE);
    }

    @Recover
    public void doMediaCutRecover(MediaCutException e, File watermarkVideo, VideoType type, CuttingSetting cuttingSetting, Long uploadId) {
        try {
            File filmFile = ffmpegService.mediaCut(watermarkVideo, type, cuttingSetting, false);
            if (FileUtil.exist(filmFile)) {
                return;
            }
        } catch (Exception ignored) {
        }
        log.error("重试后{}剪切仍然失败, uploadId:{}, filePath:{}", type == VideoType.TRAILER ? "预告片" : "短视频", uploadId, watermarkVideo.getAbsolutePath(), e);
        fileService.updateSubStatus(uploadId, type == VideoType.TRAILER ? UploadSubStatusEnum.CUT_TRAILER_FAILURE : UploadSubStatusEnum.CUT_SHORT_FAILURE);
    }

    @Recover
    public void doSingleVideoUploadRecover(UploadSingleVideo2CloudException e, File video, VideoType type, Long uploadId) {
        log.error("重试后{}上传仍然失败, uploadId:{}, filePath:{}", type == VideoType.ORIGIN_VIDEO ? "原片" : type == VideoType.TRAILER ? "预告片" : "短视频", uploadId, video.getAbsolutePath(), e);
        fileService.updateSubStatus(uploadId,
                type == VideoType.ORIGIN_VIDEO ? UploadSubStatusEnum.UPLOAD_ORIGIN_FAILURE :
                        type == VideoType.TRAILER ? UploadSubStatusEnum.UPLOAD_TRAILER_FAILURE :
                                type == VideoType.SHORT_VIDEO ? UploadSubStatusEnum.UPLOAD_SHORT_FAILURE : null);
    }

    @Recover
    public void doCaptureAndUploadScreenshotRecover(CaptureAndUploadScreenshotException e, File originVideo, Long uploadId) {
        log.error("重试后截取和上传封面到云存储且更新数据表仍然失败, uploadId:{}, folderPath:{}", uploadId, originVideo.getAbsolutePath(), e);
        fileService.updateUploadStatus(uploadId, UploadStatusEnum.CAPTURE_FAILURE);
    }

    private File saveMultipartFileInDisk(MultipartFile multipartFile, String newFilename, Long uploadId) throws IOException {
        File folder = MediaHelper.getUploadIdDir(uploadId, MgfsPath.MgfsPathType.MAIN);
        FileUtil.mkdir(folder);
        File localFile = new File(folder, newFilename);
        FileCopyUtils.copy(multipartFile.getInputStream(), Files.newOutputStream(localFile.toPath()));
        return localFile;
    }
}