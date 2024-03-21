package co.saiyan.file.service.exception;

import co.saiyan.common.model.exception.ErrorCodeEnum;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadFilm2CloudException
 */
public class UploadFilm2CloudException extends RuntimeException {

    private ErrorCodeEnum code;

    public UploadFilm2CloudException() {
        super();
    }

    public UploadFilm2CloudException(String message) {
        super(message);
    }

    public UploadFilm2CloudException(int code, String message) {
        super(code + "-" + message);
    }

    public UploadFilm2CloudException(ErrorCodeEnum code) {
        super(code.getCode() + "-" + code.getMessage());
        this.code = code;
    }

    public UploadFilm2CloudException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadFilm2CloudException(ErrorCodeEnum code, Throwable cause) {
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