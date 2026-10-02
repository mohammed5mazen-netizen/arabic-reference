package com.mrsoft.arabicreference.shared.web.error;

import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ServiceUnavailableException;
import com.mrsoft.arabicreference.shared.kernel.exception.UnauthorizedException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.kernel.trace.TraceIds;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final TimeProvider timeProvider;

    public GlobalExceptionHandler(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return respond(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, exception.getMessage(), List.of(), List.of());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNoHandler(NoHandlerFoundException exception) {
        return respond(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                "The requested resource was not found.",
                List.of(exception.getRequestURL()),
                List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException exception) {
        return respond(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                "The requested resource was not found.",
                List.of(exception.getResourcePath()),
                List.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception) {
        return respond(HttpStatus.CONFLICT, ErrorCode.CONFLICT, exception.getMessage(), List.of(), List.of());
    }

    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(ValidationException exception) {
        return respond(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                exception.getMessage(),
                List.of(),
                exception.fieldErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleBeanValidation(MethodArgumentNotValidException exception) {
        List<FieldErrorDetail> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()))
                .toList();
        return respond(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "Validation failed.", List.of(), fieldErrors);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenOperationException exception) {
        return respond(HttpStatus.FORBIDDEN, exception.code(), exception.getMessage(), List.of(), List.of());
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException exception) {
        return respond(HttpStatus.UNAUTHORIZED, exception.code(), exception.getMessage(), List.of(), List.of());
    }

    @ExceptionHandler(RateLimitedException.class)
    ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitedException exception) {
        return respond(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.RATE_LIMITED, exception.getMessage(), List.of(), List.of());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleUnavailable(ServiceUnavailableException exception) {
        return respond(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.SERVICE_UNAVAILABLE, exception.getMessage(), List.of(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return respond(
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN_OPERATION,
                "You do not have permission to perform this operation.",
                List.of(),
                List.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ApiErrorResponse> handleStaleUpdate(OptimisticLockingFailureException exception) {
        return respond(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                "The record was updated by someone else. Reload and try again.",
                List.of(),
                List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.warn("Data integrity conflict traceId={}", TraceIds.current());
        return respond(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                "The request conflicts with an existing record.",
                List.of(),
                List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return respond(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "A request value has the wrong format.", List.of(), List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
        return respond(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, "The request body could not be read.", List.of(), List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return respond(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED,
                "This method is not allowed.",
                List.of(),
                List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unhandled error traceId={}", TraceIds.current(), exception);
        return respond(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred.",
                List.of(),
                List.of());
    }

    private ResponseEntity<ApiErrorResponse> respond(
            HttpStatus status,
            ErrorCode code,
            String message,
            List<String> details,
            List<FieldErrorDetail> fieldErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
                code.name(),
                message,
                details,
                fieldErrors,
                TraceIds.current(),
                timeProvider.now());
        return ResponseEntity.status(status).body(body);
    }
}
