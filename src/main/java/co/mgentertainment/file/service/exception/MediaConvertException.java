package co.mgentertainment.file.service.exception;

import co.mgentertainment.common.model.exception.ErrorCodeEnum;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description MediaConvertException
 */
public class MediaConvertException extends RuntimeException {

    private ErrorCodeEnum code;

    public MediaConvertException() {
        super();
    }

    public MediaConvertException(String message) {
        super(message);
    }

    public MediaConvertException(int code, String message) {
        super(code + "-" + message);
    }

    public MediaConvertException(ErrorCodeEnum code) {
        super(code.getCode() + "-" + code.getMessage());
        this.code = code;
    }

    public MediaConvertException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediaConvertException(ErrorCodeEnum code, Throwable cause) {
        super(code.getCode() + "-" + code.getMessage(), cause);
        this.code = code;
    }

    public ErrorCodeEnum getCode() {
        return code;
    }

    public void setCode(ErrorCodeEnum code) {
        this.code = code;
    }
}