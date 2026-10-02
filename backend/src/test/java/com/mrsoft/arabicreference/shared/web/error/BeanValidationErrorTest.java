package com.mrsoft.arabicreference.shared.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.shared.infrastructure.time.UtcTimeProvider;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class BeanValidationErrorTest {

    @Test
    void fieldErrorsDoNotEchoRejectedValues() throws Exception {
        Method method = Sample.class.getDeclaredMethod("accept", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult("secret-value", "request");
        binding.addError(new FieldError("request", "name", "secret-value", false, null, null, "must not be blank"));

        ResponseEntity<ApiErrorResponse> response = new GlobalExceptionHandler(new UtcTimeProvider())
                .handleBeanValidation(new MethodArgumentNotValidException(parameter, binding));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().fieldErrors()).containsExactly(new FieldErrorDetail("name", "must not be blank"));
        assertThat(response.getBody().toString()).doesNotContain("secret-value");
    }

    private static final class Sample {
        @SuppressWarnings("unused")
        void accept(String name) {
        }
    }
}
