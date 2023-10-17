package co.mgentertainment.file.service.impl;

import cn.hutool.core.collection.ListUtil;
import co.mgentertainment.common.model.media.ResourcePathType;
import co.mgentertainment.common.model.media.ResourceSuffix;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.common.model.media.WatermarkPosition;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.config.WatermarkSetting;
import co.mgentertainment.file.service.utils.MediaHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description FfmpegServiceImpl
 */
@Service
@Slf4j
public class FfmpegServiceImpl implements FfmpegService {

    private final Map<String, FFmpegProbeResult> mediaMetadataCache = Maps.newConcurrentMap();

    private FFprobe ffprobe;
    private FFmpeg ffmpeg;

    @Resource
    private MgfsProperties mgfsProperties;

    public FfmpegServiceImpl() throws IOException {
        this.ffprobe = new FFprobe("ffprobe");
        this.ffmpeg = new FFmpeg("ffmpeg");
    }

    @Override
    public Integer getMediaDuration(File file) {
        try {
            return new Double(getMediaMetadata(file).getFormat().duration).intValue();
        } catch (Exception e) {
            return Integer.valueOf(0);
        }
    }

    @Override
    public File mediaConvert(@NotNull File inputFile, boolean isStableMode) {
//        FFmpegProbeResult mediaMetadata = getMediaMetadata(inputFile);
//        final List<FFmpegStream> streams = mediaMetadata.getStreams().stream().filter(fFmpegStream -> fFmpegStream.codec_type != null).collect(Collectors.toList());
//        final Optional<FFmpegStream> audioStream = streams.stream().filter(fFmpegStream -> FFmpegStream.CodecType.AUDIO.equals(fFmpegStream.codec_type)).findFirst();
        File outFile = MediaHelper.getProcessedFileByOriginFile(inputFile, VideoType.FEATURE_FILM.getValue(), ResourceSuffix.FEATURE_FILM);
        List<String> extraArgs = ListUtil.of("-force_key_frames", "expr:gte(t,n_forced*2)",
                "-hls_time", mgfsProperties.getSegmentTimeLength() + "",
                "-hls_list_size", "0",
                "-hls_flags", "0",
                "-threads", Runtime.getRuntime().availableProcessors() + "");
        if (!isStableMode) {
            extraArgs.addAll(ListUtil.of("-c:v", "copy", "-c:a", "copy"));
        }
        boolean enabled = mgfsProperties.getWatermark().isEnabled();
        if (enabled && mgfsProperties.getWatermark().getPosition() != null) {
            Integer pos = mgfsProperties.getWatermark().getPosition();
            WatermarkPosition position = WatermarkPosition.getByCode(pos);
            extraArgs.addAll(getWatermarkArgsByPosition(position));
        }
        FFmpegBuilder builder = new FFmpegBuilder().addInput(inputFile.getAbsolutePath());
        if (StringUtils.isNotEmpty(mgfsProperties.getWatermark().getWatermarkImgPath())) {
            builder.addInput(mgfsProperties.getWatermark().getWatermarkImgPath());
        }
        FFmpegOutputBuilder outputBuilder = builder
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
//                .setAudioBitRate(audioStream.map(fFmpegStream -> fFmpegStream.bit_rate).orElse(0L))
                .setAudioCodec("aac")
//                .setAudioSampleRate(audioStream.get().sample_rate)
//                .setVideoBitRate(64000)
                .setStrict(FFmpegBuilder.Strict.NORMAL)
                .setFormat("hls")
//                .setPreset("ultrafast")
                .addExtraArgs(extraArgs.toArray(new String[0]));
        if (isStableMode) {
            outputBuilder.setPreset("ultrafast");
        }
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(outputBuilder.done()).run();
        return outFile;
    }

    @Override
    public File mediaCut(File inputFile, VideoType type, CuttingSetting cuttingSetting) {
        Preconditions.checkArgument(inputFile != null && inputFile.exists() && inputFile.isFile(), "inputFile is not a file");
        Preconditions.checkArgument(type != null && cuttingSetting != null, "VideoType and CuttingSetting can not be null");
        FFmpegProbeResult mediaMetadata = getMediaMetadata(inputFile);
        double duration = mediaMetadata.getFormat().duration;
        Integer startFromProportion = type == VideoType.TRAILER ? cuttingSetting.getTrailerStartFromProportion() :
                type == VideoType.SHORT_VIDEO ? cuttingSetting.getShortVideoStartFromProportion() : null;
        log.debug("the media {} duration:{}, startFromProportion:{}", inputFile.getAbsolutePath(), duration, startFromProportion);
        long startOffset = new BigDecimal(Optional.ofNullable(duration).orElse(0.0))
                .multiply(new BigDecimal(Optional.ofNullable(startFromProportion).orElse(0)))
                .divide(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        final List<FFmpegStream> streams = mediaMetadata.getStreams().stream().filter(fFmpegStream -> fFmpegStream.codec_type != null).collect(Collectors.toList());
//        final Optional<FFmpegStream> audioStream = streams.stream().filter(fFmpegStream -> FFmpegStream.CodecType.AUDIO.equals(fFmpegStream.codec_type)).findFirst();
        String suffix = type == VideoType.TRAILER ? ResourceSuffix.TRAILER : type == VideoType.SHORT_VIDEO ? ResourceSuffix.SHORT : ".mp4";
        File outFile = MediaHelper.getProcessedFileByOriginFile(inputFile, type.getValue(), suffix);
        Integer cutDuration = type == VideoType.TRAILER ? cuttingSetting.getTrailerDuration() :
                type == VideoType.SHORT_VIDEO ? cuttingSetting.getShortVideoDuration() : 0;
        FFmpegBuilder builder = new FFmpegBuilder()
                .setStartOffset(startOffset, TimeUnit.SECONDS)
                .setInput(inputFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
                .setDuration(cutDuration, TimeUnit.SECONDS)
//                .setAudioBitRate(audioStream.map(fFmpegStream -> fFmpegStream.bit_rate).orElse(0L))
                .setAudioCodec("aac")
//                .setAudioSampleRate(audioStream.get().sample_rate)
//                .setVideoBitRate(64000)
                .setStrict(FFmpegBuilder.Strict.NORMAL)
                .setFormat("mp4")
//                .setPreset("ultrafast")
                .addExtraArgs(
                        "-force_key_frames", "expr:gte(t,n_forced*2)",
                        "-c:v", "copy",
                        "-c:a", "copy",
//                        "-vsync", "2",
//                        "-tune", "fastdecode",
                        "-threads", Runtime.getRuntime().availableProcessors() + "")
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        // remove cache
        mediaMetadataCache.remove(inputFile.getAbsolutePath());
        return outFile;
    }

    @Override
    public File mediaConcat(File inputFile, File subFilesTxt) {
        File outputFile = MediaHelper.getProcessedFileByOriginFile(inputFile, VideoType.FEATURE_FILM.getValue(), ResourceSuffix.FEATURE_FILM);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(subFilesTxt.getAbsolutePath())
                .setFormat("concat")
                .addExtraArgs("-safe", "0")
                .overrideOutputFiles(true) // Override the output if it exists
                .addOutput(outputFile.getAbsolutePath()) // Filename for the destination
                .disableSubtitle() // No subtiles
                .setAudioChannels(1) // Mono audio
                .setAudioCodec("aac") // using the aac codec
                .setAudioSampleRate(48_000) // at 48KHz
                .setAudioBitRate(32768) // at 32 kbit/s
                .setVideoCodec("libx264") // Video using x264
                .setVideoFrameRate(24, 1) // at 24 frames per second
                .setVideoResolution(1080, 720) // at 640x480 resolution
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                .done();
        builder.setVerbosity(FFmpegBuilder.Verbosity.DEBUG);
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        // Run a one-pass encode
        executor.createJob(builder).run();
        return outputFile;
    }

    @Override
    public File captureScreenshot(File videoFile) {
        File coverFile = MediaHelper.getCoverFileFromInputFile(videoFile);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(videoFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(coverFile.getAbsolutePath())
                .setFormat("image2")
                .addExtraArgs(
                        "-vf", "select=eq(pict_type\\,I)",
                        "-frames:v", "1",
                        "-pix_fmt", "yuvj422p",
                        "-vsync", "vfr",
                        "-qscale:v", "2")
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return coverFile;
    }

    @Override
    public File printWatermark(File inputFile) {
        WatermarkSetting setting = mgfsProperties.getWatermark();
        if (setting == null || !setting.isEnabled() || StringUtils.isEmpty(setting.getWatermarkImgPath())) {
            return inputFile;
        }
        List<String> extraArgs = new ArrayList<>();
        extraArgs.addAll(getWatermarkArgsByPosition(WatermarkPosition.getByCode(Optional.ofNullable(mgfsProperties.getWatermark().getPosition()).orElse(WatermarkPosition.BOTTOM_RIGHT.getCode()))));
        File outFile = MediaHelper.getProcessedFileByOriginFile(inputFile, ResourcePathType.FEATURE_FILM.getValue(), ResourceSuffix.ORIGIN_FILM);
        FFmpegBuilder builder = new FFmpegBuilder()
                .addInput(inputFile.getAbsolutePath())
                .addInput(mgfsProperties.getWatermark().getWatermarkImgPath())
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
                .addExtraArgs(extraArgs.toArray(new String[0]))
                .setFormat("mp4")
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return outFile;
    }

    private List<String> getWatermarkArgsByPosition(WatermarkPosition position) {
        List<String> extraArgs = new ArrayList<>();
        switch (position) {
            case TOP_LEFT:
                extraArgs.addAll(ListUtil.of("-filter_complex", "overlay=x=0:y=0"));
                break;
            case TOP_RIGHT:
                extraArgs.addAll(ListUtil.of("-filter_complex", "overlay=x=main_w-overlay_w:y=0"));
                break;
            case BOTTOM_LEFT:
                extraArgs.addAll(ListUtil.of("-filter_complex", "overlay=x=0:y=main_h-overlay_h"));
                break;
            case BOTTOM_RIGHT:
                extraArgs.addAll(ListUtil.of("-filter_complex", "overlay=x=main_w-overlay_w:y=main_h-overlay_h"));
                break;
            default:
                extraArgs.addAll(ListUtil.of("-filter_complex", "overlay=x=main_w-overlay_w:y=main_h-overlay_h"));
                break;
        }
        return extraArgs;
    }

    private FFmpegProbeResult getMediaMetadata(@NotNull File file) {
        return mediaMetadataCache.computeIfAbsent(file.getAbsolutePath(), s -> {
            try {
                return ffprobe.probe(file.getAbsolutePath());
            } catch (IOException e) {
                log.error("getMediaMetadata error", e);
                return null;
            }
        });
    }

}
