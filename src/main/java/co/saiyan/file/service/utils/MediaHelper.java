package co.saiyan.file.service.utils;

import cn.hutool.core.io.FileUtil;
import co.saiyan.common.model.media.ResourcePathType;
import co.saiyan.common.model.media.ResourceSuffix;
import co.saiyan.common.model.media.VideoType;
import co.saiyan.file.service.config.CuttingSetting;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author larry
 * @createTime 2023/9/20
 * @description MediaHelper
 */
public class MediaHelper {

    private static final String INVALID_REGEX = "[\\\\/:*!#?【】？！，。〖〗\"<>| ]";

    public static BigDecimal getMediaSize(long sizeInBytes) {
        return new BigDecimal(sizeInBytes).divide(new BigDecimal(1024)).setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatMediaDuration(Integer seconds) {
        if (seconds == null) {
            return "00:00:00";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    public static File getCoverFileFromInputFile(File inputFile) {
        File newDir = new File(inputFile.getParentFile(), ResourcePathType.COVER.getValue());
        FileUtil.mkdir(newDir);
        return new File(newDir, ResourceSuffix.SCREENSHOT);
    }

    public static File getUploadIdDir(Long uploadId, String path) {
        return new File(path, String.valueOf(uploadId));
    }

    public static File getProcessedFileByOriginFile(File inputFile, String folderName, String fileSuffix) {
        File newDir = new File(inputFile.getParentFile(), folderName);
        FileUtil.mkdir(newDir);
        String filename = StringUtils.substringBeforeLast(inputFile.getName(), ".");
        String newFilename = filename + fileSuffix;
        return new File(newDir, newFilename);
    }

    public static void deleteCompletedVideoFolder(Long uploadId, String path) {
        File folderToDelete = getUploadIdDir(uploadId, path);
        if (folderToDelete != null && folderToDelete.exists()) {
            FileUtil.del(folderToDelete.getAbsolutePath());
        }
    }

    public static void clearCutSettingAfterCut(VideoType videoType, CuttingSetting cuttingSetting) {
        switch (videoType) {
            case TRAILER:
                cuttingSetting.setTrailerDuration(null);
                break;
            case SHORT_VIDEO:
                cuttingSetting.setShortVideoDuration(null);
                break;
            default:
                break;
        }
    }

    public static String filterInvalidFilenameChars(String originFilename) {
        return originFilename.replaceAll(INVALID_REGEX, StringUtils.EMPTY);
    }

    public static File renameInvalidFile(File f) {
        if (!FileUtil.exist(f)) {
            return f;
        }
        String filename = f.getName().replaceAll(INVALID_REGEX, StringUtils.EMPTY);
        FileUtil.rename(f, filename, false);
        return new File(f.getParentFile(), filename);
    }

    public static String getUploadIdFilename(String originFilename, Long uploadId) {
        return uploadId + "." + StringUtils.substringAfterLast(originFilename, '.');
    }

    public static File renameUploadIdFile(File f, Long uploadId) {
        if (!FileUtil.exist(f)) {
            return f;
        }
        String filename = getUploadIdFilename(f.getName(), uploadId);
        FileUtil.rename(f, filename, false);
        return new File(f.getParentFile(), filename);
    }

    public static File moveFileToUploadDir(File srcFile, Long uploadId, String path) {
        File targetDir = MediaHelper.getUploadIdDir(uploadId, path);
        FileUtil.mkdir(targetDir);
        File targetFile = new File(targetDir, srcFile.getName());
        FileUtil.move(srcFile, targetFile, true);
        return targetFile;
    }
}
