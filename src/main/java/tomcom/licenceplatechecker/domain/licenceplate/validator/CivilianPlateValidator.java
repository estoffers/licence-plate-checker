package tomcom.licenceplatechecker.domain.licenceplate.validator;

import tomcom.licenceplatechecker.domain.licenceplate.Distinguisher;
import tomcom.licenceplatechecker.domain.licenceplate.LicencePlate;

import java.util.Optional;
import java.util.Set;

/**
 * Validator for German civilian licence plates.
 * <p>
 * Civilian plates are the standard licence plates used by private citizens and businesses.
 * Format: [Distinguisher Code]-[Identifier][Number][Modifier]
 * <ul>
 *   <li>Distinguisher: 1-3 letters (validated separately)</li>
 *   <li>Identifier: 0-2 letters (no umlauts, no forbidden combinations)</li>
 *   <li>Number: 1-4 digits</li>
 *   <li>Modifier: Optional 'H' (historical) or 'E' (electric)</li>
 * </ul>
 * </p>
 */
public class CivilianPlateValidator {

    private static final int MAX_TOTAL_LENGTH = 8;
    private static final int MAX_IDENTIFIER_LENGTH = 2;
    private static final int MAX_NUMBER_LENGTH = 4;
    private static final String DIGITS_ONLY_REGEX = "[0-9]+";
    
    private static final Set<Character> FORBIDDEN_UMLAUT_CHARS = Set.of('Ä', 'Ö', 'Ü');

    private final RedPlateValidator dealerPlateValidator;

    public CivilianPlateValidator() {
        this.dealerPlateValidator = new RedPlateValidator();
    }


    public Optional<LicencePlate> validate(Distinguisher distinguisher, String remainingPart, String modifier) {
        if (remainingPart.isEmpty()) {
            return Optional.empty();
        }

        IdentifierExtractionResult identifierResult = extractIdentifier(remainingPart);
        String identifier = identifierResult.identifier();
        String number = identifierResult.remainingString();

        // Handle dealer plate: no identifier, number starts with "06"
        if (identifier.isEmpty()) {
            return dealerPlateValidator.validate(distinguisher, number, modifier);
        }

        return validateStandardPlate(distinguisher, identifier, number, modifier);
    }

    private IdentifierExtractionResult extractIdentifier(String input) {
        StringBuilder identifier = new StringBuilder();
        int position = 0;

        while (position < input.length() 
               && Character.isAlphabetic(input.charAt(position)) 
               && identifier.length() < MAX_IDENTIFIER_LENGTH) {
            
            char currentChar = input.charAt(position);
            
            if (FORBIDDEN_UMLAUT_CHARS.contains(currentChar)) {
                return new IdentifierExtractionResult("", input);
            }
            
            identifier.append(currentChar);
            position++;
        }

        String remaining = input.substring(position);
        return new IdentifierExtractionResult(identifier.toString(), remaining);
    }

    private Optional<LicencePlate> validateStandardPlate(Distinguisher distinguisher, String identifier, String number, String modifier) {
        if (identifier.length() > MAX_IDENTIFIER_LENGTH) {
            return Optional.empty();
        }

        if (!number.matches(DIGITS_ONLY_REGEX) || number.length() > MAX_NUMBER_LENGTH) {
            return Optional.empty();
        }

        int totalLength = distinguisher.code.length() + identifier.length() + number.length() + modifier.length();
        if (totalLength > MAX_TOTAL_LENGTH) {
            return Optional.empty();
        }

        return Optional.of(LicencePlate.of(distinguisher, identifier, number, modifier));
    }

    private record IdentifierExtractionResult(String identifier, String remainingString) {}
}
