package co.saiyan.file.web.interceptor;

import co.saiyan.file.service.config.MgfsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author larry
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
        if (!mgfsProperties.getApplyAccess().isEnabled()) {
            return true;
        }
        log.info("当前请求来源：{}", request.getRemoteAddr());
        return mgfsProperties.getApplyAccess().getWhiteList().stream().anyMatch(subnet -> new IpAddressMatcher(subnet).matches(request));
    }
}