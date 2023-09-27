package co.mgentertainment.file.service.exception;

import co.mgentertainment.common.model.exception.ErrorCodeEnum;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadTrailer2CloudException
 */
public class UploadTrailer2CloudException extends RuntimeException {

    private ErrorCodeEnum code;

    public UploadTrailer2CloudException() {
        super();
    }

    public UploadTrailer2CloudException(String message) {
        super(message);
    }

    public UploadTrailer2CloudException(int code, String message) {
        super(code + "-" + message);
    }

    public UploadTrailer2CloudException(ErrorCodeEnum code) {
        super(code.getCode() + "-" + code.getMessage());
        this.code = code;
    }

    public UploadTrailer2CloudException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadTrailer2CloudException(ErrorCodeEnum code, Throwable cause) {
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