package sn.samabank.transaction.shared;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final String detail;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
        this.detail = null;
    }

    public BusinessException(String code, String message, HttpStatus status, String detail) {
        super(message);
        this.code = code;
        this.status = status;
        this.detail = detail;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
    public String getDetail() { return detail; }

    public static BusinessException notFound(String resource, Object id) {
        return new BusinessException("NOT_FOUND", resource + " introuvable : " + id, HttpStatus.NOT_FOUND);
    }

    public static BusinessException unprocessable(String code, String message, String detail) {
        return new BusinessException(code, message, HttpStatus.UNPROCESSABLE_ENTITY, detail);
    }
}
