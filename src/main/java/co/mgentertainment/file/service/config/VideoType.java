package co.mgentertainment.file.service.config;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description VideoType
 */
public enum VideoType {
    FEATURE_FILM("film"),
    TRAILER("trailer"),
    SHORT_VIDEO("short");

    private final String value;

    VideoType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
