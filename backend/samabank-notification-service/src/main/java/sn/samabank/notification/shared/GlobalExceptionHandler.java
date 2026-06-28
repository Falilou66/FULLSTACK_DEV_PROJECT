package sn.samabank.notification.shared;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        List<ApiError.FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getCode(), fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(
                ApiError.builder()
                        .correlationId(getCorrelationId(request))
                        .timestamp(Instant.now())
                        .status(400)
                        .code("VALIDATION_FAILED")
                        .message("Donnees invalides")
                        .fieldErrors(errors)
                        .build()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex,
                                                   HttpServletRequest request) {
        log.warn("[BUSINESS] {} - correlationId: {}", ex.getMessage(), getCorrelationId(request));

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiError.builder()
                        .correlationId(getCorrelationId(request))
                        .timestamp(Instant.now())
                        .status(ex.getStatus().value())
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .detail(ex.getDetail())
                        .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex,
                                               HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiError.builder()
                        .correlationId(getCorrelationId(request))
                        .timestamp(Instant.now())
                        .status(401)
                        .code("UNAUTHORIZED")
                        .message("Authentification requise")
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiError.builder()
                        .correlationId(getCorrelationId(request))
                        .timestamp(Instant.now())
                        .status(403)
                        .code("ACCESS_DENIED")
                        .message("Acces non autorise")
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex, HttpServletRequest request) {
        String correlationId = getCorrelationId(request);
        log.error("[ERROR] correlationId: {} - {}", correlationId, ex.getMessage(), ex);

        return ResponseEntity.internalServerError().body(
                ApiError.builder()
                        .correlationId(correlationId)
                        .timestamp(Instant.now())
                        .status(500)
                        .code("INTERNAL_ERROR")
                        .message("Erreur interne. Reference : " + correlationId)
                        .build()
        );
    }

    private String getCorrelationId(HttpServletRequest request) {
        return Optional
                .ofNullable(request.getHeader(CORRELATION_ID_HEADER))
                .orElse("unknown");
    }
}
