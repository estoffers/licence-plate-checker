package tomcom.licenceplatechecker.domain.validator;

import tomcom.licenceplatechecker.domain.LicencePlate;
import tomcom.licenceplatechecker.domain.Distinguisher;

import java.util.Optional;

/**
 * Validator for German THW (Technisches Hilfswerk) licence plates.
 * <p>
 * THW plates are used by the Federal Agency for Technical Relief.
 * </p>
 * <p>
 * Rules:
 * <ul>
 *   <li>4 or 5 digit numbers</li>
 *   <li>Must start with "8" or "9"</li>
 *   <li>Valid ranges:
 *     <ul>
 *       <li>8000-8999 (4 digits)</li>
 *       <li>80000-89999 (5 digits)</li>
 *       <li>9000-9999 (4 digits)</li>
 *       <li>90000-99999 (5 digits)</li>
 *     </ul>
 *   </li>
 *   <li>Special: THW-8000 to THW-8009 reserved for THW Federal Headquarters</li>
 *   <li>Special: THW-0600 to THW-0699 are red test drive plates</li>
 *   <li>No modifiers allowed (H/E)</li>
 *   <li>No identifier letters</li>
 * </ul>
 * </p>
 * <p>
 * Valid examples: THW8234, THW-85000, THW-9456, THW-0650<br>
 * Invalid examples: THW7000 (wrong prefix), THW800 (too short), THW8234H (modifier)
 * </p>
 */
public class ThwValidator {

    private static final int RED_PLATE_MIN = 600;
    private static final int RED_PLATE_MAX = 699;
    public Optional<LicencePlate> validate(Distinguisher distinguisher, String remainingPart, String modifier) {
        // THW plates do not allow modifiers
        if (!modifier.isEmpty()) {
            return Optional.empty();
        }

        // Must be only digits
        if (!remainingPart.matches("[0-9]+")) {
            return Optional.empty();
        }

        // Check if it's a red dealer plate (THW-0600 to THW-0699)
        if (isRedPlate(remainingPart)) {
            return Optional.of(LicencePlate.of(distinguisher, "", remainingPart, ""));
        }

        // Regular THW plates: must be 4 or 5 digits
        int length = remainingPart.length();
        if (length != 4 && length != 5) {
            return Optional.empty();
        }

        // Must start with 8 or 9
        char firstDigit = remainingPart.charAt(0);
        if (firstDigit != '8' && firstDigit != '9') {
            return Optional.empty();
        }

        // Validate specific ranges
        int number = Integer.parseInt(remainingPart);

        if (length == 4) {
            // 4-digit: 8000-8999 or 9000-9999
            if ((number >= 8000 && number <= 8999) || (number >= 9000 && number <= 9999)) {
                return Optional.of(LicencePlate.of(distinguisher, "", remainingPart, ""));
            }
        } else { // length == 5
            // 5-digit: 80000-89999 or 90000-99999
            if ((number >= 80000 && number <= 89999) || (number >= 90000 && number <= 99999)) {
                return Optional.of(LicencePlate.of(distinguisher, "", remainingPart, ""));
            }
        }

        return Optional.empty();
    }

    /**
     * Checks if the number is a THW red dealer plate.
     * THW has special red plates in the range 0600-0699 for test drives.
     * 
     * @param number the number to check
     * @return true if it's a valid THW red plate number
     */
    private boolean isRedPlate(String number) {
        if (!number.matches("[0-9]{4}")) {
            return false;
        }

        int value = Integer.parseInt(number);
        return value >= RED_PLATE_MIN && value <= RED_PLATE_MAX;
    }
}
