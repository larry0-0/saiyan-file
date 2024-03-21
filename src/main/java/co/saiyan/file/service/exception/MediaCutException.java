package co.saiyan.file.service.exception;

import co.saiyan.common.model.exception.ErrorCodeEnum;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description MediaCutException
 */
public class MediaCutException extends RuntimeException {

    private ErrorCodeEnum code;

    public MediaCutException() {
        super();
    }

    public MediaCutException(String message) {
        super(message);
    }

    public MediaCutException(int code, String message) {
        super(code + "-" + message);
    }

    public MediaCutException(ErrorCodeEnum code) {
        super(code.getCode() + "-" + code.getMessage());
        this.code = code;
    }

    public MediaCutException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediaCutException(ErrorCodeEnum code, Throwable cause) {
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