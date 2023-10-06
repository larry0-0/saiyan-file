package co.mgentertainment.file.service.config;

import co.mgentertainment.common.fileupload.tika.ContentTypeDetect;
import co.mgentertainment.common.fileupload.tika.TikaFactory;
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
