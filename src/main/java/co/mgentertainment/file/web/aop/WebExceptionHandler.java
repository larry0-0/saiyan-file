package co.mgentertainment.file.web.aop;

import co.mgentertainment.common.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author leo
 * @createTime 2023/7/7
 * @description ControllerExceptionHandler
 */
@RestControllerAdvice
@Slf4j
public class WebExceptionHandler {

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public R<Void> handleMethodException(Exception ex) {
        log.error("server error:{}", ex.getMessage());
        return R.failed(ex.getMessage());
    }
}
