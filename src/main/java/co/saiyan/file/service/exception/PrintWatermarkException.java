package co.saiyan.file.service.exception;

import co.saiyan.common.model.exception.ErrorCodeEnum;

/**
 * @author larry
 * @createTime 2023/9/26
 * @description PrintWatermarkException
 */
public class PrintWatermarkException extends RuntimeException {

    private ErrorCodeEnum code;

    public PrintWatermarkException() {
        super();
    }

    public PrintWatermarkException(String message) {
        super(message);
    }

    public PrintWatermarkException(int code, String message) {
        super(code + "-" + message);
    }

    public PrintWatermarkException(ErrorCodeEnum code) {
        super(code.getCode() + "-" + code.getMessage());
        this.code = code;
    }

    public PrintWatermarkException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrintWatermarkException(ErrorCodeEnum code, Throwable cause) {
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