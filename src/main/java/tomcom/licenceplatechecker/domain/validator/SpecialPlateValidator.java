package tomcom.licenceplatechecker.domain.validator;

import tomcom.licenceplatechecker.domain.LicencePlate;
import tomcom.licenceplatechecker.domain.Distinguisher;

import java.util.Optional;

/**
 * Coordinator validator for German special licence plates.
 * <p>
 * Delegates validation to specific validators based on the distinguisher code.
 * Each special plate type has its own dedicated validator with specific rules.
 * </p>
 * <p>
 * Supported special plate types:
 * <ul>
 *   <li><strong>Y</strong> - Bundeswehr (German Armed Forces)</li>
 *   <li><strong>X</strong> - NATO forces</li>
 *   <li><strong>BP</strong> - Federal Police (Bundespolizei)</li>
 *   <li><strong>THW</strong> - Technical Relief Agency (Technisches Hilfswerk)</li>
 *   <li>Other special codes follow generic validation</li>
 * </ul>
 * </p>
 */
public class SpecialPlateValidator {

    private static final int MAX_NUMBER_LENGTH_DEFAULT = 6;

    private final BundeswehrValidator bundeswehrValidator;
    private final NatoValidator natoValidator;
    private final FederalPoliceValidator federalPoliceValidator;
    private final ThwValidator thwValidator;

    public SpecialPlateValidator() {
        this.bundeswehrValidator = new BundeswehrValidator();
        this.natoValidator = new NatoValidator();
        this.federalPoliceValidator = new FederalPoliceValidator();
        this.thwValidator = new ThwValidator();
    }

    public Optional<LicencePlate> validate(Distinguisher distinguisher, String remainingPart, String modifier) {
        return switch (distinguisher.code) {
            case "Y" -> bundeswehrValidator.validate(distinguisher, remainingPart, modifier);
            case "X" -> natoValidator.validate(distinguisher, remainingPart, modifier);
            case "BP" -> federalPoliceValidator.validate(distinguisher, remainingPart, modifier);
            case "THW" -> thwValidator.validate(distinguisher, remainingPart, modifier);
            default -> validateGenericSpecial(distinguisher, remainingPart, modifier);
        };
    }

    private Optional<LicencePlate> validateGenericSpecial(Distinguisher distinguisher, String remainingPart, String modifier) {
        // Generic special plates don't allow modifiers
        if (!modifier.isEmpty()) {
            return Optional.empty();
        }

        if (!isValidNumericOnly(remainingPart)) {
            return Optional.empty();
        }

        return Optional.of(LicencePlate.of(distinguisher, "", remainingPart, ""));
    }

    private boolean isValidNumericOnly(String number) {
        if (number == null || number.isEmpty()) {
            return false;
        }

        if (!number.matches("[0-9]+")) {
            return false;
        }

        return number.length() <= SpecialPlateValidator.MAX_NUMBER_LENGTH_DEFAULT;
    }
}
