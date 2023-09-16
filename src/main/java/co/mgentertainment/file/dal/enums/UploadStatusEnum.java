package co.mgentertainment.file.dal.enums;

import java.util.Arrays;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description UploadStatusEnum
 */
public enum UploadStatusEnum {

    TO_CONVERT(0, "待转换"),
    TO_UPLOAD(1, "待上传"),
    COMPLETED(2, "已完成"),
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
