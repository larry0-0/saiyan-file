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

    Integer getMediaDuration(File file);

    File mediaConvert(File inputFile, boolean isStableMode);

    File mediaCut(File inputFile, VideoType type, CuttingSetting cuttingSetting);

    File mediaConcat(File inputFile, File subFilesTxt);

    File captureScreenshot(File videoFile);

    File printWatermark(File inputFile);
}
