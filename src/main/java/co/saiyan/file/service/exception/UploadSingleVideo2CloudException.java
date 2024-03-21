package co.saiyan.file.service.exception;

import co.saiyan.common.model.exception.ErrorCodeEnum;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description UploadTrailer2CloudException
 */
public class UploadSingleVideo2CloudException extends RuntimeException {

    private ErrorCodeEnum code;

    public UploadSingleVideo2CloudException() {
        super();
    }

    public UploadSingleVideo2CloudException(String message) {
        super(message);
    }

    public UploadSingleVideo2CloudException(int code, String message) {
        super(code + "-" + message);
    }

    public UploadSingleVideo2CloudException(ErrorCodeEnum code) {
        super(code.getCode() + "-" + code.getMessage());
        this.code = code;
    }

    public UploadSingleVideo2CloudException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadSingleVideo2CloudException(ErrorCodeEnum code, Throwable cause) {
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