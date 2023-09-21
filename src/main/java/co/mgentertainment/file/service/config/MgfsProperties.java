package co.mgentertainment.file.service.config;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/16
 * @description FileServiceProperties
 */
@Configuration
@ConfigurationProperties(prefix = "mgfs")
@Data
public class MgfsProperties {

    /**
     * 访问认证
     */
    private Authentication authentication = new Authentication();

    /**
     * 访问白名单
     */
    private List<String> whiteList = Lists.newArrayList();

    /**
     * 文件存储地址加密
     */
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
    public static class Authentication {
        private boolean enabled;
        /**
         * 加密算法
         */
        private String algorithm = StringUtils.EMPTY;

        /**
         * rsa公钥
         */
        private String rsaPublicKey = StringUtils.EMPTY;

        /**
         * rsa私钥
         */
        private String rsaPrivateKey = StringUtils.EMPTY;

        /**
         * aes密钥
         */
        private String aesSecret = StringUtils.EMPTY;
    }

    @Data
    public static class AddressEncryption {
        private boolean enabled;
        /**
         * 文件存储地址加密密钥
         */
        private String secret = StringUtils.EMPTY;
    }

    public enum AlgorithmType {
        RSA,
        AES
    }
}
