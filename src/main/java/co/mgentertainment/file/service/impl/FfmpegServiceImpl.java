package co.mgentertainment.file.service.impl;

import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.model.media.ResourcePathType;
import co.mgentertainment.common.model.media.ResourceSuffix;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.common.model.media.WatermarkPosition;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.config.WatermarkSetting;
import co.mgentertainment.file.service.utils.MediaHelper;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
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
    public File mediaConvert(@NotNull File inputFile, boolean disabledWatermark, boolean fastMode) {
//        FFmpegProbeResult mediaMetadata = getMediaMetadata(inputFile);
//        final List<FFmpegStream> streams = mediaMetadata.getStreams().stream().filter(fFmpegStream -> fFmpegStream.codec_type != null).collect(Collectors.toList());
//        final Optional<FFmpegStream> audioStream = streams.stream().filter(fFmpegStream -> FFmpegStream.CodecType.AUDIO.equals(fFmpegStream.codec_type)).findFirst();
        Boolean isGpu = mgfsProperties.getGpuBased();
        File outFile = MediaHelper.getProcessedFileByOriginFile(inputFile, VideoType.FEATURE_FILM.getValue(), ResourceSuffix.FEATURE_FILM);
        List<String> extraArgs = new ArrayList<>();
        if (!isGpu) {
            extraArgs = disabledWatermark && fastMode ?
                    Lists.newArrayList("-c:v", COPY_STREAM_CODEC, "-c:a", COPY_STREAM_CODEC) :
                    Lists.newArrayList("-c:v", DEFAULT_CODEC);
        }
        Integer duration = getMediaDuration(inputFile);
        int segmentTimeLength = duration != null && duration.intValue() < mgfsProperties.getSegmentTimeLength() ? mgfsProperties.getSegmentTimeLength() / 3 : mgfsProperties.getSegmentTimeLength();
        extraArgs.addAll(Lists.newArrayList(
                "-threads", Runtime.getRuntime().availableProcessors() + "",
                "-force_key_frames", "expr:gte(t,n_forced*2)",
                "-hls_time", segmentTimeLength + "",
                "-hls_list_size", "0",
                "-hls_flags", "0"));
        boolean supportWatermark = mgfsProperties.getWatermark().isEnabled();
        if (!disabledWatermark && supportWatermark && mgfsProperties.getWatermark().getPosition() != null) {
            Integer pos = mgfsProperties.getWatermark().getPosition();
            Integer marginX = mgfsProperties.getWatermark().getMarginX();
            Integer marginY = mgfsProperties.getWatermark().getMarginY();
            WatermarkPosition position = WatermarkPosition.getByCode(pos);
            extraArgs.addAll(getWatermarkArgsByPosition(position, marginX, marginY));
        }
        FFmpegBuilder builder = new FFmpegBuilder();
        if (isGpu) {
            builder.addExtraArgs("-c:v", NVIDIA_CODEC);
        }
        builder.addInput(inputFile.getAbsolutePath());
        if (!disabledWatermark && supportWatermark && StringUtils.isNotEmpty(mgfsProperties.getWatermark().getWatermarkImgPath())) {
            builder.addInput(mgfsProperties.getWatermark().getWatermarkImgPath());
        }
        FFmpegOutputBuilder outputBuilder = builder
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
                .addExtraArgs(extraArgs.toArray(new String[0]))
//                .setAudioBitRate(audioStream.map(fFmpegStream -> fFmpegStream.bit_rate).orElse(0L))
//                .setAudioCodec("aac")
//                .setAudioSampleRate(audioStream.get().sample_rate)
//                .setVideoBitRate(64000)
//                .setVideoCodec("h264")
//                .setStrict(FFmpegBuilder.Strict.NORMAL)
                .setFormat("hls");
        if (!fastMode) {
            outputBuilder.setPreset("ultrafast");
        }
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(outputBuilder.done()).run();
        return outFile;
    }

    // 设置为空时默认截取前十秒
    @Override
    public File mediaCut(File inputFile, VideoType type, CuttingSetting cuttingSetting, boolean fastMode) {
        Preconditions.checkArgument(FileUtil.exist(inputFile) && inputFile.isFile(), "inputFile is not a file");
        Preconditions.checkArgument(type != null && cuttingSetting != null, "VideoType and CuttingSetting can not be null");
        FFmpegProbeResult mediaMetadata = getMediaMetadata(inputFile);
        double duration = mediaMetadata.getFormat().duration;
        Integer startFromProportion = type == VideoType.TRAILER ? cuttingSetting.getTrailerStartFromProportion() :
                type == VideoType.SHORT_VIDEO ? cuttingSetting.getShortVideoStartFromProportion() : null;
        log.debug("the media {} duration:{}, startFromProportion:{}", inputFile.getAbsolutePath(), duration, startFromProportion);
        long startOffset = new BigDecimal(Optional.ofNullable(duration).orElse(10.0))
                .multiply(new BigDecimal(Optional.ofNullable(startFromProportion).orElse(0)))
                .divide(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValue();
//        final List<FFmpegStream> streams = mediaMetadata.getStreams().stream().filter(fFmpegStream -> fFmpegStream.codec_type != null).collect(Collectors.toList());
//        final Optional<FFmpegStream> audioStream = streams.stream().filter(fFmpegStream -> FFmpegStream.CodecType.AUDIO.equals(fFmpegStream.codec_type)).findFirst();
        String suffix = type == VideoType.TRAILER ? ResourceSuffix.TRAILER : type == VideoType.SHORT_VIDEO ? ResourceSuffix.SHORT : ".mp4";
        File outFile = MediaHelper.getProcessedFileByOriginFile(inputFile, type.getValue(), suffix);
        Integer cutDuration = type == VideoType.TRAILER ? cuttingSetting.getTrailerDuration() :
                type == VideoType.SHORT_VIDEO ? cuttingSetting.getShortVideoDuration() : null;
        Boolean isGpu = mgfsProperties.getGpuBased();
        List<String> extraArgs = new ArrayList<>();
        if (!isGpu) {
            extraArgs = fastMode ?
                    Lists.newArrayList("-c:v", COPY_STREAM_CODEC, "-c:a", COPY_STREAM_CODEC) :
                    Lists.newArrayList("-c:v", DEFAULT_CODEC);
        }
        extraArgs.addAll(Lists.newArrayList(
                "-threads", Runtime.getRuntime().availableProcessors() + "",
                "-force_key_frames", "expr:gte(t,n_forced*2)"));
        FFmpegBuilder builder = new FFmpegBuilder();
        if (isGpu) {
            builder.addExtraArgs("-c:v", NVIDIA_CODEC);
        }
        FFmpegOutputBuilder outputBuilder = builder.setInput(inputFile.getAbsolutePath())
                .setStartOffset(startOffset, TimeUnit.SECONDS)
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
                .setDuration(Optional.ofNullable(cutDuration).orElse(mgfsProperties.getUserTrailerTimeLength()), TimeUnit.SECONDS)
                .addExtraArgs(extraArgs.toArray(new String[0]))
//                .setAudioBitRate(audioStream.map(fFmpegStream -> fFmpegStream.bit_rate).orElse(0L))
//                .setVideoCodec("h264")
//                .setAudioCodec("aac")
//                .setAudioSampleRate(audioStream.get().sample_rate)
//                .setVideoBitRate(64000)
//                .setStrict(FFmpegBuilder.Strict.NORMAL)
                .setFormat("mp4");
        if (!fastMode) {
            outputBuilder.setPreset("ultrafast");
        }
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(outputBuilder.done()).run();
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
                .setVideoCodec("h264") // Video using x264
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
        Boolean isGpu = mgfsProperties.getGpuBased();
        List<String> extraArgs = new ArrayList<>();
        if (!isGpu) {
            extraArgs = Lists.newArrayList("-c:v", DEFAULT_CODEC);
        }
        extraArgs.addAll(Lists.newArrayList("-threads", Runtime.getRuntime().availableProcessors() + ""));
        extraArgs.addAll(getWatermarkArgsByPosition(WatermarkPosition.getByCode(Optional.ofNullable(setting.getPosition()).orElse(WatermarkPosition.BOTTOM_RIGHT.getCode())), setting.getMarginX(), setting.getMarginY()));
        File outFile = MediaHelper.getProcessedFileByOriginFile(inputFile, ResourcePathType.ORIGIN.getValue(), ResourceSuffix.ORIGIN_FILM);

        FFmpegBuilder builder = new FFmpegBuilder();
        if (isGpu) {
            builder.addExtraArgs("-c:v", NVIDIA_CODEC);
        }
        builder
                .addInput(inputFile.getAbsolutePath())
                .addInput(mgfsProperties.getWatermark().getWatermarkImgPath())
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
                .addExtraArgs(extraArgs.toArray(new String[0]))
                .setPreset("ultrafast")
                .setFormat("mp4")
                .done();
//                .setVideoCodec("h264")
//                .setAudioCodec("aac")
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return outFile;
    }

    private List<String> getWatermarkArgsByPosition(WatermarkPosition position, Integer marginX, Integer marginY) {
        List<String> extraArgs = new ArrayList<>();
        marginX = Optional.ofNullable(marginX).orElse(0);
        marginY = Optional.ofNullable(marginY).orElse(0);
        switch (position) {
            case TOP_LEFT:
                extraArgs.addAll(Lists.newArrayList("-filter_complex", "overlay=x=" + marginX + ":y=" + marginY));
                break;
            case TOP_RIGHT:
                extraArgs.addAll(Lists.newArrayList("-filter_complex", "overlay=x=main_w-overlay_w-" + marginX + ":y=" + marginY));
                break;
            case BOTTOM_LEFT:
                extraArgs.addAll(Lists.newArrayList("-filter_complex", "overlay=x=" + marginX + ":y=main_h-overlay_h-" + marginY));
                break;
            case BOTTOM_RIGHT:
                extraArgs.addAll(Lists.newArrayList("-filter_complex", "overlay=x=main_w-overlay_w-" + marginX + ":y=main_h-overlay_h-" + marginY));
                break;
            default:
                extraArgs.addAll(Lists.newArrayList("-filter_complex", "overlay=x=main_w-overlay_w:y=main_h-overlay_h"));
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
