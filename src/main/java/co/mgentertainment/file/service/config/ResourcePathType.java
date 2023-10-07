package co.mgentertainment.file.service.config;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description VideoType
 */
public enum ResourcePathType {
    DEFAULT("default"),
    IMAGE("image"),
    THUMBNAIL("thumbnail"),
    FEATURE_FILM("film"),
    TRAILER("trailer"),
    SHORT("short");

    private final String value;

    ResourcePathType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
