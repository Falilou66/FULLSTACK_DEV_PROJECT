package sn.samabank.account.shared;

public class ApiError {
    private final String code;
    private final String message;
    private final String detail;

    public ApiError(String code, String message, String detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getDetail() { return detail; }
}
