package co.mgentertainment.file.service.utils;

import cn.hutool.core.io.FileUtil;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.VideoType;

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

    public static File getUploadIdDir(Long uploadId) {
        return new File("/data/mgfs/" + uploadId.toString());
    }

    public static File getProcessedFileByOriginFile(File inputFile, String folderName, String fileSuffix) {
        String filename = org.apache.commons.lang.StringUtils.substringBeforeLast(inputFile.getName(), ".");
        File newDir = new File(inputFile.getParentFile(), folderName);
        FileUtil.mkdir(newDir);
        String newFilename = filename + fileSuffix;
        return new File(newDir, newFilename);
    }

    public static void deleteCompletedVideoFolder(Long uploadId) {
        File folderToDelete = getUploadIdDir(uploadId);
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
}
