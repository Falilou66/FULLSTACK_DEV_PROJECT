package sn.samabank.account.shared;

import java.time.Instant;

public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String correlationId;
    private final Instant timestamp;

    private ApiResponse(boolean success, T data, String correlationId) {
        this.success = success;
        this.data = data;
        this.correlationId = correlationId;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok(T data, String correlationId) {
        return new ApiResponse<>(true, data, correlationId);
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getCorrelationId() { return correlationId; }
    public Instant getTimestamp() { return timestamp; }
}
