package tomcom.licenceplatechecker.domain;

import org.springframework.stereotype.Service;
import tomcom.licenceplatechecker.domain.exception.AmbiguousLicencePlateException;
import tomcom.licenceplatechecker.domain.exception.InvalidLicencePlateException;
import tomcom.licenceplatechecker.domain.validator.CivilianPlateValidator;
import tomcom.licenceplatechecker.domain.validator.SpecialPlateValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class LicencePlateValidationService {

    private static final String ALLOWED_CHARACTERS_REGEX = "[A-Z0-9\\- ]+";
    private static final String ALPHANUMERIC_ONLY_REGEX = "[A-Z0-9]+";
    private static final String REGION_CODE_REGEX = "[A-Z]{1,3}";

    private static final int MAX_REGION_CODE_LENGTH = 3;
    private static final Set<Character> VALID_MODIFIERS = Set.of('H', 'E');

    private final RegionRepository regionRepository;
    private final SpecialPlateValidator specialPlateValidator;
    private final CivilianPlateValidator civilianPlateValidator;

    public LicencePlateValidationService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
        this.specialPlateValidator = new SpecialPlateValidator();
        this.civilianPlateValidator = new CivilianPlateValidator();
    }

    public LicencePlate validateLicencePlate(String input) {
        validateInput(input);
        String normalizedInput = normalizeCase(input);

        if (containsSeparators(normalizedInput)) {
            Optional<LicencePlate> result = tryParseWithExplicitSeparator(normalizedInput);
            if (result.isPresent()) {
                return result.get();
            }
        }

        return parseAmbiguous(normalizedInput);
    }

    /**
     * Validate basic input constraints.
     */
    private void validateInput(String input) {
        if (input == null || input.isBlank()) {
            throw new InvalidLicencePlateException("Kennzeichen darf nicht leer sein");
        }
    }

    /**
     * Normalize input to uppercase for consistent processing.
     */
    private String normalizeCase(String input) {
        String normalized = input.toUpperCase(Locale.ROOT).trim();

        if (!normalized.matches(ALLOWED_CHARACTERS_REGEX)) {
            throw new InvalidLicencePlateException(
                "Nur Buchstaben A-Z, Ziffern 0-9 sowie '-' und Leerzeichen erlaubt"
            );
        }

        return normalized;
    }

    /**
     * Check if the input contains separator characters (space or hyphen).
     */
    private boolean containsSeparators(String input) {
        return input.contains("-") || input.contains(" ");
    }

    /**
     * Remove all separator characters from the input.
     */
    private String removeSeparators(String input) {
        return input.replace("-", "").replace(" ", "");
    }

    /**
     * Try to parse the input using explicit separators to identify the region boundary.
     * This method provides unambiguous parsing when separators are present.
     */
    private Optional<LicencePlate> tryParseWithExplicitSeparator(String input) {
        // Find first separator to split region from the rest
        int separatorIndex = findFirstSeparatorIndex(input);
        if (separatorIndex <= 0) {
            return Optional.empty();
        }

        String regionCode = input.substring(0, separatorIndex).trim();
        if (!regionCode.matches(REGION_CODE_REGEX)) {
            return Optional.empty();
        }

        Optional<Region> regionOpt = regionRepository.findByCode(regionCode);
        if (regionOpt.isEmpty()) {
            return Optional.empty();
        }

        Region region = regionOpt.get();
        String remainingPart = extractRemainingPart(input, separatorIndex);

        return parseRemainingPart(region, remainingPart);
    }

    /**
     * Parse licence plate when no separators are present or separator-based parsing failed.
     * This method may result in ambiguous results when multiple valid interpretations exist.
     */
    private LicencePlate parseAmbiguous(String input) {
        String normalized = removeSeparators(input);

        if (!normalized.matches(ALPHANUMERIC_ONLY_REGEX)) {
            throw new InvalidLicencePlateException("Nur Buchstaben A-Z und Ziffern 0-9 erlaubt");
        }

        ModifierExtractionResult modifierResult = extractTrailingModifier(normalized);
        String workingString = modifierResult.remainingString();

        List<Region> regionCandidates = findRegionCandidates(workingString);

        if (regionCandidates.isEmpty()) {
            throw new InvalidLicencePlateException("Unbekanntes Regional-Kürzel");
        }

        List<LicencePlate> validParsings = new ArrayList<>();

        for (Region region : regionCandidates) {
            String remainingPart = workingString.substring(region.code.length());
            parseRemainingPart(region, remainingPart + modifierResult.modifier())
                .ifPresent(validParsings::add);
        }

        return selectUniqueParsing(validParsings);
    }

    /**
     * Parse the remaining part of a licence plate after the region code.
     * Delegate to appropriate validator based on region type (special or civilian).
     */
    private Optional<LicencePlate> parseRemainingPart(Region region, String remainingPart) {
        ModifierExtractionResult modifierResult = extractTrailingModifier(remainingPart.trim());
        String workingString = modifierResult.remainingString();
        String modifier = modifierResult.modifier();
        String cleanedString = removeSeparators(workingString);

        if (Boolean.TRUE.equals(region.special)) {
            return specialPlateValidator.validate(region, cleanedString, modifier);
        }

        return civilianPlateValidator.validate(region, cleanedString, modifier);
    }

    /**
     * Extract modifier from the end of a string
     */
    private ModifierExtractionResult extractTrailingModifier(String input) {
        if (input.isEmpty()) {
            return new ModifierExtractionResult(input, "");
        }

        char lastChar = input.charAt(input.length() - 1);
        if (VALID_MODIFIERS.contains(lastChar)) {
            return new ModifierExtractionResult(
                input.substring(0, input.length() - 1).trim(),
                String.valueOf(lastChar)
            );
        }

        return new ModifierExtractionResult(input, "");
    }

    /**
     * Find all valid region codes that could be the prefix of the input
     */
    private List<Region> findRegionCandidates(String input) {
        List<Region> candidates = new ArrayList<>();
        int maxLength = Math.min(MAX_REGION_CODE_LENGTH, input.length());

        for (int length = 1; length <= maxLength; length++) {
            String potentialCode = input.substring(0, length);
            regionRepository.findByCode(potentialCode).ifPresent(candidates::add);
        }

        return candidates;
    }

    /**
     * Select a unique parsing result or throws an exception if ambiguous or invalid.
     */
    private LicencePlate selectUniqueParsing(List<LicencePlate> parsings) {
        if (parsings.isEmpty()) {
            throw new InvalidLicencePlateException("Ungültiges Kennzeichen");
        }

        if (parsings.size() > 1) {
            throw new AmbiguousLicencePlateException("Kennzeichen mehrdeutig");
        }

        return parsings.get(0);
    }

    /**
     * Find the index of the first separator (space or hyphen) in the string
     */
    private int findFirstSeparatorIndex(String input) {
        int hyphenIndex = input.indexOf('-');
        int spaceIndex = input.indexOf(' ');

        if (hyphenIndex == -1) return spaceIndex;
        if (spaceIndex == -1) return hyphenIndex;

        return Math.min(hyphenIndex, spaceIndex);
    }

    /**
     * Extract the remaining part after the region code, removing leading separators.
     */
    private String extractRemainingPart(String input, int separatorIndex) {
        String remaining = input.substring(separatorIndex + 1).trim();

        // Remove any leading separators
        while (!remaining.isEmpty() && isSeparator(remaining.charAt(0))) {
            remaining = remaining.substring(1).trim();
        }

        return remaining;
    }

    /**
     * Checks if a character is a separator (space or hyphen).
     */
    private boolean isSeparator(char c) {
        return c == '-' || c == ' ';
    }

    // Helper record for cleaner return values
    private record ModifierExtractionResult(String remainingString, String modifier) {}
}