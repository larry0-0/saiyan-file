package co.mgentertainment.file.web.config;

import co.mgentertainment.file.web.interceptor.AuthTokenInterceptor;
import co.mgentertainment.file.web.interceptor.WhitelistAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static co.mgentertainment.file.web.controller.AccessClientController.TOKEN_HEADER;

/**
 * @author larry
 * @createTime 2023/6/15
 * @description WebMvcConfig
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String API_SERVICE_PREFIX = "/api/v1";
    private static final String BASE_PACKAGE = "co.mgentertainment.file.web.controller";

    private final WhitelistAccessInterceptor whitelistAccessInterceptor;
    private final AuthTokenInterceptor authTokenInterceptor;

    /**
     * allow CORS requests
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                // 暴露 header 中的其他属性给客户端应用程序
                .exposedHeaders(TOKEN_HEADER)
                .maxAge(3600);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_SERVICE_PREFIX, HandlerTypePredicate.forBasePackage(BASE_PACKAGE))
                // setUseTrailingSlashMatch,设置是否自动后缀留级匹配模式，如“/user”是否匹配“/user/”，为true匹配
                .setUseTrailingSlashMatch(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(whitelistAccessInterceptor).addPathPatterns(API_SERVICE_PREFIX + "/access/**");
        registry.addInterceptor(authTokenInterceptor).addPathPatterns(API_SERVICE_PREFIX + "/file/**");
    }
}