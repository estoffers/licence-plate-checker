package tomcom.licenceplatechecker.domain.validator;

import tomcom.licenceplatechecker.domain.LicencePlate;
import tomcom.licenceplatechecker.domain.Distinguisher;

import java.util.Optional;

/**
 * Validator for German NATO (X) licence plates.
 * <p>
 * NATO plates are used by NATO forces stationed in Germany.
 * </p>
 * <p>
 * Rules:
 * <ul>
 *   <li>Only digits allowed (no letters)</li>
 *   <li>Minimum 1 digit, maximum 6 digits</li>
 *   <li>No identifier letters</li>
 *   <li>No modifiers allowed (H/E)</li>
 * </ul>
 * </p>
 * <p>
 * Valid examples: X1, X123, X123456<br>
 * Invalid examples: X1234567 (too long), XAB123 (letters), X123H (modifier)
 * </p>
 */
public class NatoValidator {

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
