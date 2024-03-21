package co.saiyan.file.service.config;

import lombok.Data;

/**
 * @author larry
 * @createTime 2023/10/17
 * @description WatermarkSetting
 */
@Data
public class WatermarkSetting {

    // Whether to enable watermark
    private boolean enabled;

    // Watermark image path
    private String watermarkImgPath;

    // WatermarkPosition code
    private Integer position;

    // Watermark margin-x
    private Integer marginX;

    // Watermark margin-y
    private Integer marginY;
}
