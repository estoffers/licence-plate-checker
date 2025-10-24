package tomcom.licenceplatechecker.domain.exception;

public class AmbiguousLicencePlateException extends RuntimeException {
    public AmbiguousLicencePlateException(String message) {
        super(message);
    }
}
