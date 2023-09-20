package co.mgentertainment.file.service;

import co.mgentertainment.file.service.config.CuttingSetting;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description FfmpegUtils
 */
public interface FfmpegService {

    FFmpegProbeResult getMediaMetadata(File file);

    File mediaConvert(File inputFile);

    File mediaCut(File inputFile, CuttingSetting cuttingSetting);

    File mediaConcat(File inputFile, File subFilesTxt);

}
