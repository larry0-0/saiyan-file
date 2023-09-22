package co.mgentertainment.file.service.impl;

import cn.hutool.core.io.FileUtil;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.config.ResourceSuffix;
import co.mgentertainment.file.service.dto.MediaCutResultDTO;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
        return new Double(getMediaMetadata(file).getFormat().duration).intValue();
    }

    @Override
    public File mediaConvert(@NotNull File inputFile) {
        FFmpegProbeResult mediaMetadata = getMediaMetadata(inputFile);
        final List<FFmpegStream> streams = mediaMetadata.getStreams().stream().filter(fFmpegStream -> fFmpegStream.codec_type != null).collect(Collectors.toList());
        final Optional<FFmpegStream> audioStream = streams.stream().filter(fFmpegStream -> FFmpegStream.CodecType.AUDIO.equals(fFmpegStream.codec_type)).findFirst();
        File outFile = getOutputFileFromInputFile(inputFile, "_hls", ResourceSuffix.FEATURE_FILM);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
                .setAudioBitRate(audioStream.map(fFmpegStream -> fFmpegStream.bit_rate).orElse(0L))
                .setAudioCodec("aac")
                .setAudioSampleRate(audioStream.get().sample_rate)
                .setVideoBitRate(64000)
                .setStrict(FFmpegBuilder.Strict.NORMAL)
                .setFormat("hls")
                .setPreset("ultrafast")
                .addExtraArgs("-vsync", "2",
                        "-force_key_frames", "expr:gte(t,n_forced*2)",
                        "-c:v", "copy",
                        "-c:a", "copy",
                        "-tune", "fastdecode",
                        "-hls_time", mgfsProperties.getSegmentTimeLength() + "",
                        "-hls_list_size", "0",
                        "-hls_flags", "0",
                        "-threads", Runtime.getRuntime().availableProcessors() + "")
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return outFile;
    }

    @Override
    public MediaCutResultDTO mediaCut(File inputFile, CuttingSetting cuttingSetting) {
        FFmpegProbeResult mediaMetadata = getMediaMetadata(inputFile);
        double duration = mediaMetadata.getFormat().duration;
        long startOffset = new BigDecimal(duration).multiply(new BigDecimal(cuttingSetting.getStartFromProportion())).divide(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).longValue();
        final List<FFmpegStream> streams = mediaMetadata.getStreams().stream().filter(fFmpegStream -> fFmpegStream.codec_type != null).collect(Collectors.toList());
        final Optional<FFmpegStream> audioStream = streams.stream().filter(fFmpegStream -> FFmpegStream.CodecType.AUDIO.equals(fFmpegStream.codec_type)).findFirst();
        File outFile = getOutputFileFromInputFile(inputFile, "_trailer", ResourceSuffix.TRAILER);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setStartOffset(startOffset, TimeUnit.SECONDS)
                .setInput(inputFile.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(outFile.getAbsolutePath())
                .setDuration(cuttingSetting.getDuration(), TimeUnit.SECONDS)
                .setAudioBitRate(audioStream.map(fFmpegStream -> fFmpegStream.bit_rate).orElse(0L))
                .setAudioCodec("aac")
                .setAudioSampleRate(audioStream.get().sample_rate)
                .setVideoBitRate(64000)
                .setStrict(FFmpegBuilder.Strict.NORMAL)
                .setFormat("mp4")
                .setPreset("ultrafast")
                .addExtraArgs("-vsync", "2",
                        "-force_key_frames", "expr:gte(t,n_forced*2)",
                        "-c:v", "copy",
                        "-c:a", "copy",
                        "-tune", "fastdecode",
                        "-threads", Runtime.getRuntime().availableProcessors() + "")
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return MediaCutResultDTO.builder().trailerFile(outFile).filmDuration(new BigDecimal(duration).setScale(0, RoundingMode.HALF_UP).intValue()).build();
    }

    @Override
    public File mediaConcat(File inputFile, File subFilesTxt) {
        File outputFile = getOutputFileFromInputFile(inputFile, "_hls", ResourceSuffix.FEATURE_FILM);
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

    private FFmpegProbeResult getMediaMetadata(@NotNull File file) {
        try {
            return ffprobe.probe(file.getAbsolutePath());
        } catch (Exception e) {
            log.error("getMediaMetadata error", e);
        }
        return null;
    }

    private File getOutputFileFromInputFile(File inputFile, String folderSuffix, String fileSuffix) {
        String filename = StringUtils.substringBeforeLast(inputFile.getName(), ".");
        String newFolderName = new StringBuffer(filename).append('.').append(RandomStringUtils.randomAlphanumeric(4)).append(folderSuffix).toString();
        File newDir = new File(inputFile.getParentFile(), newFolderName);
        if (!newDir.exists()) {
            FileUtil.mkdir(newDir);
        }
        String newFilename = filename + fileSuffix;
        return new File(newDir, newFilename);
    }
}
