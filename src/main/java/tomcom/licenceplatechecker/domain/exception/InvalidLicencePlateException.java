package tomcom.licenceplatechecker.domain.exception;

public class InvalidLicencePlateException extends RuntimeException {
    public InvalidLicencePlateException(String message) {
        super(message);
    }
}
