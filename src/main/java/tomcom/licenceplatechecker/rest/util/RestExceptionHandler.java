package tomcom.licenceplatechecker.rest.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tomcom.licenceplatechecker.domain.licenceplate.exception.AmbiguousLicencePlateException;
import tomcom.licenceplatechecker.domain.licenceplate.exception.InvalidLicencePlateException;

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(InvalidLicencePlateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalid(InvalidLicencePlateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AmbiguousLicencePlateException.class)
    public ResponseEntity<ApiResponse<Void>> handleAmbiguous(AmbiguousLicencePlateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(ex.getMessage()));
    }
}
