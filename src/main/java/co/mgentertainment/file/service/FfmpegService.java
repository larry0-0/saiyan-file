package co.mgentertainment.file.service;

import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.config.CuttingSetting;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description FfmpegUtils
 */
public interface FfmpegService {
    String NVIDIA_CODEC = "h264_cuvid";
    String DEFAULT_CODEC = "libx264";
    String COPY_STREAM_CODEC = "copy";

    Integer getMediaDuration(File file);

    File mediaConvert(File inputFile, boolean disabledWatermark, boolean fastMode, boolean isShortVideo);

    File mediaCut(File inputFile, VideoType type, CuttingSetting cuttingSetting, boolean fastMode);

    File mediaConcat(File inputFile, File subFilesTxt);

    File captureScreenshot(File videoFile);

    File printWatermark(File inputFile);
}
