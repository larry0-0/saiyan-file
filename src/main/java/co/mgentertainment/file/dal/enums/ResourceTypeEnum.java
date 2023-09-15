package co.mgentertainment.file.dal.enums;

import java.util.Arrays;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description ResourceTypeEnum
 */
public enum ResourceTypeEnum {

    VIDEO(0),
    IMAGE(1),
    PACKAGE(2),
    OTHER(100);

    private final Integer value;

    ResourceTypeEnum(int value) {
        this.value = value;
    }

    public static ResourceTypeEnum getByValue(Integer value) {
        return Arrays.stream(ResourceTypeEnum.values()).filter(item -> item.value.equals(value)).findFirst().orElse(OTHER);
    }

    public int getValue() {
        return value;
    }
}
