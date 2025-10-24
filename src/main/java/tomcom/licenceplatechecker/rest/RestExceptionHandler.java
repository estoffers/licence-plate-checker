package tomcom.licenceplatechecker.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tomcom.licenceplatechecker.domain.exception.AmbiguousLicencePlateException;
import tomcom.licenceplatechecker.domain.exception.InvalidLicencePlateException;

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(InvalidLicencePlateException.class)
    public ResponseEntity<String> handleInvalid(InvalidLicencePlateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AmbiguousLicencePlateException.class)
    public ResponseEntity<String> handleAmbiguous(AmbiguousLicencePlateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }
}
