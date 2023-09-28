package co.mgentertainment.file.web.interceptor;

import co.mgentertainment.common.utils.SecurityHelper;
import co.mgentertainment.file.dal.repository.AccessClientRepository;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.web.cache.ClientHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
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
public class AuthTokenInterceptor implements HandlerInterceptor {

    private final MgfsProperties mgfsProperties;

    private final AccessClientRepository accessClientRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!mgfsProperties.getAuthentication().isEnabled()) {
            return true;
        }
        String token = request.getHeader(mgfsProperties.getApiToken());
        String algorithm = mgfsProperties.getAuthentication().getAlgorithm();
        String appCode;
        if (MgfsProperties.AlgorithmType.RSA.name().equalsIgnoreCase(algorithm)) {
            appCode = SecurityHelper.rsaPeriodDecrypt(token, mgfsProperties.getAuthentication().getRsaPrivateKey(), null);
        } else {
            appCode = SecurityHelper.hyperDecrypt(token, mgfsProperties.getAuthentication().getAesSecret());
        }
        ClientHolder.setCurrentClient(appCode);
        return accessClientRepository.validateAppCode(appCode);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        ClientHolder.remove();
    }

}