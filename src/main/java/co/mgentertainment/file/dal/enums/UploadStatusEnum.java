package co.mgentertainment.file.dal.enums;

import java.util.Arrays;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description UploadStatusEnum
 */
public enum UploadStatusEnum {

    CONVERTING(0, "转码中"),
    UPLOADING(1, "上传中"),
    TRAILER_CUTTING_AND_UPLOADING(2, "预告片剪切上传中"),
    COMPLETED(10, "已完成"),
    ;

    private final Integer value;
    private final String desc;

    UploadStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static UploadStatusEnum getByValue(Integer value) {
        return Arrays.stream(UploadStatusEnum.values()).filter(item -> item.value.equals(value)).findFirst().orElse(null);
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
