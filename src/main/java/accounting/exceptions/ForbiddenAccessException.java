package accounting.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "The requested user not owner or not administrator")
public class ForbiddenAccessException extends RuntimeException {
}
