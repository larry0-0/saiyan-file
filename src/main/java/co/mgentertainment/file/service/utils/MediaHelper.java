package co.mgentertainment.file.service.utils;

import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.model.media.MgfsPath;
import co.mgentertainment.common.model.media.ResourcePathType;
import co.mgentertainment.common.model.media.ResourceSuffix;
import co.mgentertainment.common.model.media.VideoType;
import co.mgentertainment.file.service.config.CuttingSetting;
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

    public static File getUploadIdDir(Long uploadId, MgfsPath.MgfsPathType pathType) {
        return new File(pathType.getValue(), String.valueOf(uploadId));
    }

    public static File getProcessedFileByOriginFile(File inputFile, String folderName, String fileSuffix) {
        File newDir = new File(inputFile.getParentFile(), folderName);
        FileUtil.mkdir(newDir);
        String filename = StringUtils.substringBeforeLast(inputFile.getName(), ".");
        String newFilename = filename + fileSuffix;
        return new File(newDir, newFilename);
    }

    public static void deleteCompletedVideoFolder(Long uploadId, MgfsPath.MgfsPathType pathType) {
        File folderToDelete = getUploadIdDir(uploadId, pathType);
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
        return originFilename.replaceAll("[\\\\/:*!#?【】？！〖〗\"<>|]", StringUtils.EMPTY);
    }

    public static File renameInvalidFile(File f) {
        if (!FileUtil.exist(f)) {
            return f;
        }
        String filename = f.getName().replaceAll("[\\\\/:*!#?【】？！〖〗\"<>|]", StringUtils.EMPTY);
        FileUtil.rename(f, filename, false);
        return new File(f.getParentFile(), filename);
    }

    public static File moveFileToUploadDir(File srcFile, Long uploadId, MgfsPath.MgfsPathType pathType) {
        File targetDir = MediaHelper.getUploadIdDir(uploadId, pathType);
        FileUtil.mkdir(targetDir);
        File targetFile = new File(targetDir, srcFile.getName());
        FileUtil.move(srcFile, targetFile, true);
        return targetFile;
    }
}
