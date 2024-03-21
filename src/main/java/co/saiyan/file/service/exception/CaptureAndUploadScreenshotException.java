package co.saiyan.file.service.exception;

import co.saiyan.common.model.exception.ErrorCodeEnum;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description CaptureAndUploadScreenshotException
 */
public class CaptureAndUploadScreenshotException extends RuntimeException {

    private ErrorCodeEnum code;

    public CaptureAndUploadScreenshotException() {
        super();
    }

    public CaptureAndUploadScreenshotException(String message) {
        super(message);
    }

    public CaptureAndUploadScreenshotException(int code, String message) {
        super(code + "-" + message);
    }

    public CaptureAndUploadScreenshotException(ErrorCodeEnum code) {
        super(code.getCode() + "-" + code.getMessage());
        this.code = code;
    }

    public CaptureAndUploadScreenshotException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaptureAndUploadScreenshotException(ErrorCodeEnum code, Throwable cause) {
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