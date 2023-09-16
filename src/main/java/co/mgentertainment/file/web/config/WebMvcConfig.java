package co.mgentertainment.file.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author larry
 * @createTime 2023/6/15
 * @description WebMvcConfig
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String WEB_SERVICE_PREFIX = "/api/v1";
    private static final String BASE_PACKAGE = "co.mgentertainment.file.web.controller";

    /**
     * allow CORS requests
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                // 暴露 header 中的其他属性给客户端应用程序
                .exposedHeaders("Authorization", "X-Requested-With", "X-Total-Count", "Link",
                        "Access-Control-Allow-Origin",
                        "Access-Control-Allow-Credentials")
                .maxAge(3600);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(WEB_SERVICE_PREFIX, HandlerTypePredicate.forBasePackage(BASE_PACKAGE))
                // setUseTrailingSlashMatch,设置是否自动后缀留级匹配模式，如“/user”是否匹配“/user/”，为true匹配
                .setUseTrailingSlashMatch(true);
    }

}