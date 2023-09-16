package co.mgentertainment.file.service.config;

import cn.xuyanwu.spring.file.storage.tika.ContentTypeDetect;
import cn.xuyanwu.spring.file.storage.tika.TikaFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author larry
 * @createTime 2023/9/15
 * @description ServiceBeanConfig
 */
@Configuration
public class ServiceBeanConfig {

    @Bean
    public TikaFactory tikaFactory() {
        return new DefaultTikaFactory();
    }

    @Bean
    public ContentTypeDetect contentTypeDetect(TikaFactory tikaFactory) {
        return new TikaContentTypeDetect(tikaFactory);
    }
}
