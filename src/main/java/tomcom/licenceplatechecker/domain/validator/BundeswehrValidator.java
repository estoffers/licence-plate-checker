package tomcom.licenceplatechecker.domain.validator;

import tomcom.licenceplatechecker.domain.LicencePlate;
import tomcom.licenceplatechecker.domain.Distinguisher;

import java.util.Optional;

public class BundeswehrValidator {

    private static final int MIN_NUMBER_LENGTH = 1;
    private static final int MAX_NUMBER_LENGTH = 6;
    private static final String VALID_NUMBER_REGEX = "[0-9]{" + MIN_NUMBER_LENGTH + "," + MAX_NUMBER_LENGTH + "}";

    public Optional<LicencePlate> validate(Distinguisher distinguisher, String remainingPart, String modifier) {
        if (!modifier.isEmpty()) {
            return Optional.empty();
        }

        if (!remainingPart.matches(VALID_NUMBER_REGEX)) {
            return Optional.empty();
        }

        return Optional.of(LicencePlate.of(distinguisher, "", remainingPart, ""));
    }
}
