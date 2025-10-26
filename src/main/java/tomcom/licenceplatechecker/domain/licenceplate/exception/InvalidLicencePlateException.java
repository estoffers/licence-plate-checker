package tomcom.licenceplatechecker.domain.licenceplate.exception;

public class InvalidLicencePlateException extends RuntimeException {
    public InvalidLicencePlateException(String message) {
        super(message);
    }
}
