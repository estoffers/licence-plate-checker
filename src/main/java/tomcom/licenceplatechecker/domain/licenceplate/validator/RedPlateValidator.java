package tomcom.licenceplatechecker.domain.licenceplate.validator;

import tomcom.licenceplatechecker.domain.licenceplate.LicencePlate;
import tomcom.licenceplatechecker.domain.licenceplate.Distinguisher;

import java.util.Optional;
import java.util.Set;

/**
 * Validator for German red licence plates (Rote Kennzeichen).
 * <p>
 * Red plates are special temporary plates used for specific purposes:
 * <ul>
 *   <li><strong>06</strong> - Dealer plates (Händlerkennzeichen): Used by car dealers for test drives and vehicle transport</li>
 *   <li><strong>07</strong> - Oldtimer plates: Used by classic car owners for events, test drives, and maintenance trips</li>
 *   <li><strong>05</strong> - Technical inspection plates: Used by inspection organizations (TÜV, DEKRA, etc.) for test drives</li>
 * </ul>
 * All red plates share these characteristics:
 * <ul>
 *   <li>No identifier letters</li>
 *   <li>Number starts with "05", "06", or "07"</li>
 *   <li>Up to 6 digits allowed</li>
 *   <li><strong>No modifiers allowed</strong> (red plates cannot have H or E suffix)</li>
 * </ul>
 * </p>
 */
public class RedPlateValidator {

    private static final Set<String> RED_PLATE_PREFIXES = Set.of("05", "06", "07");
    private static final int MAX_NUMBER_LENGTH = 6;
    private static final int MAX_TOTAL_LENGTH = 8;
    private static final String DIGITS_ONLY_REGEX = "[0-9]{1," + MAX_NUMBER_LENGTH + "}";

    public Optional<LicencePlate> validate(Distinguisher distinguisher, String number, String modifier) {
        if (!modifier.isEmpty())
            return Optional.empty();

        if (!isRedPlateNumber(number))
            return Optional.empty();

        if (!isWithinMaxLength(distinguisher, number))
            return Optional.empty();

        return Optional.of(LicencePlate.of(distinguisher, "", number, ""));
    }

    private boolean isRedPlateNumber(String number) {
        if (!number.matches(DIGITS_ONLY_REGEX)) {
            return false;
        }

        return RED_PLATE_PREFIXES.stream()
            .anyMatch(number::startsWith);
    }

    private boolean isWithinMaxLength(Distinguisher distinguisher, String number) {
        int totalLength = distinguisher.code.length() + number.length();
        return totalLength <= MAX_TOTAL_LENGTH;
    }
}