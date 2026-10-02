package testprobe;

import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/__probe")
public class ErrorProbeController {

    @GetMapping("/not-found")
    void notFound() {
        throw new ResourceNotFoundException("Missing entry");
    }

    @GetMapping("/conflict")
    void conflict() {
        throw new ConflictException("Already recorded");
    }

    @GetMapping("/validation")
    void validation() {
        throw new ValidationException("Validation failed.", List.of(new FieldErrorDetail("q", "must not be blank")));
    }

    @GetMapping("/forbidden")
    void forbidden() {
        throw new ForbiddenOperationException("Operation is not allowed.");
    }

    @GetMapping("/boom")
    void boom() {
        throw new IllegalStateException("sql-leak-marker SELECT secret_table");
    }
}
