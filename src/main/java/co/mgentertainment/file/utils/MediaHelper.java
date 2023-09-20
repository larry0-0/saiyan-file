package co.mgentertainment.file.utils;

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
}
