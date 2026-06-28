package sn.samabank.auth.shared;

import java.time.Instant;
import java.util.List;

public class ApiError {

    private final String correlationId;
    private final Instant timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String detail;
    private final List<FieldError> fieldErrors;

    private ApiError(Builder builder) {
        this.correlationId = builder.correlationId;
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.code = builder.code;
        this.message = builder.message;
        this.detail = builder.detail;
        this.fieldErrors = builder.fieldErrors;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String correlationId;
        private Instant timestamp = Instant.now();
        private int status;
        private String code;
        private String message;
        private String detail;
        private List<FieldError> fieldErrors;

        public Builder correlationId(String val) { this.correlationId = val; return this; }
        public Builder timestamp(Instant val) { this.timestamp = val; return this; }
        public Builder status(int val) { this.status = val; return this; }
        public Builder code(String val) { this.code = val; return this; }
        public Builder message(String val) { this.message = val; return this; }
        public Builder detail(String val) { this.detail = val; return this; }
        public Builder fieldErrors(List<FieldError> val) { this.fieldErrors = val; return this; }
        public ApiError build() { return new ApiError(this); }
    }

    public static class FieldError {
        private final String field;
        private final String code;
        private final String message;

        public FieldError(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }

        public String getField() { return field; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    public String getCorrelationId() { return correlationId; }
    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getDetail() { return detail; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }
}
