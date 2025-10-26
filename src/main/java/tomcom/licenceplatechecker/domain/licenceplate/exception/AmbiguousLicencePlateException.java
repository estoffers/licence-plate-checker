package tomcom.licenceplatechecker.domain.licenceplate.exception;

public class AmbiguousLicencePlateException extends RuntimeException {
    public AmbiguousLicencePlateException(String message) {
        super(message);
    }
}
