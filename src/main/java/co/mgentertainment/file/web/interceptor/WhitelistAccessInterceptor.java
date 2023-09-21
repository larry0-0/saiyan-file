package co.mgentertainment.file.web.interceptor;

import co.mgentertainment.file.service.config.MgfsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author cl
 * @createTime 2022/12/5
 * @description OpenAPI AuthenticationInterceptor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WhitelistAccessInterceptor implements HandlerInterceptor {

    private final MgfsProperties mgfsProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("当前请求来源：{}", request.getRemoteAddr());
        return mgfsProperties.getWhiteList().stream().anyMatch(subnet -> new IpAddressMatcher(subnet).matches(request));
    }
}