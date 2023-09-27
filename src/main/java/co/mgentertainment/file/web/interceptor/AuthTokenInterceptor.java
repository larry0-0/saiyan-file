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

import static co.mgentertainment.file.web.controller.AccessClientController.TOKEN_HEADER;

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
        String token = request.getHeader(TOKEN_HEADER);
        String algorithm = mgfsProperties.getAuthentication().getAlgorithm();
        String appCode;
        if (MgfsProperties.AlgorithmType.RSA.name().equalsIgnoreCase(algorithm)) {
            appCode = SecurityHelper.rsaDecrypt(token, mgfsProperties.getAuthentication().getRsaPrivateKey(), 10);
        } else {
            appCode = SecurityHelper.hyperDecrypt(token, mgfsProperties.getAuthentication().getAesSecret());
        }
        ClientHolder.setCurrentClient(appCode);
        return accessClientRepository.existsAppCode(appCode);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        ClientHolder.remove();
    }

}