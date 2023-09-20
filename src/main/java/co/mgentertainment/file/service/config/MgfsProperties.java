package co.mgentertainment.file.service.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description FileServiceProperties
 */
@Configuration
@ConfigurationProperties(prefix = "mgfs")
@Data
public class MgfsProperties {

    private AddressEncryption encryption = new AddressEncryption();

    /**
     * 分片时间长度单位秒
     */
    private int segmentTimeLength;

    /**
     * hls文件存放目录
     */
    private String hlsDir = StringUtils.EMPTY;

    @Data
    public static class AddressEncryption {
        private boolean enabled;
        /**
         * 文件存储地址加密密钥
         */
        private String secret = StringUtils.EMPTY;
    }
}
