package tomcom.licenceplatechecker.domain.licenceplate.validator;

import org.springframework.stereotype.Service;
import tomcom.licenceplatechecker.domain.licenceplate.Distinguisher;
import tomcom.licenceplatechecker.domain.licenceplate.DistinguisherRepository;
import tomcom.licenceplatechecker.domain.licenceplate.LicencePlate;
import tomcom.licenceplatechecker.domain.licenceplate.exception.AmbiguousLicencePlateException;
import tomcom.licenceplatechecker.domain.licenceplate.exception.InvalidLicencePlateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class LicencePlateValidationService {

    private static final String ALLOWED_CHARACTERS_REGEX = "[A-Z0-9\\- ]+";
    private static final String ALPHANUMERIC_ONLY_REGEX = "[A-Z0-9]+";
    private static final String DISTINGUISHER_CODE_REGEX = "[A-Z]{1,3}";

    private static final int MAX_DISTINGUISHER_CODE_LENGTH = 3;
    private static final Set<Character> VALID_MODIFIERS = Set.of('H', 'E');

    private final DistinguisherRepository distinguisherRepository;
    private final SpecialPlateValidator specialPlateValidator;
    private final CivilianPlateValidator civilianPlateValidator;

    public LicencePlateValidationService(DistinguisherRepository distinguisherRepository) {
        this.distinguisherRepository = distinguisherRepository;
        this.specialPlateValidator = new SpecialPlateValidator();
        this.civilianPlateValidator = new CivilianPlateValidator();
    }

    public LicencePlate validateLicencePlate(String input) {
        validateInput(input);
        String normalizedInput = normalizeCase(input);
        Distinguisher distinguisher = null;
        if (containsSeparators(normalizedInput))
            distinguisher = getDistinguisher(normalizedInput);
        return validateAndParseLicencePlate(normalizedInput, distinguisher);
    }

    private void validateInput(String input) {
        if (input == null || input.isBlank()) {
            throw new InvalidLicencePlateException("Kennzeichen darf nicht leer sein");
        }
    }

    private String normalizeCase(String input) {
        String normalized = input.toUpperCase(Locale.ROOT).trim();

        if (!normalized.matches(ALLOWED_CHARACTERS_REGEX)) {
            throw new InvalidLicencePlateException(
                "Nur Buchstaben A-Z, Ziffern 0-9 sowie '-' und Leerzeichen erlaubt"
            );
        }

        return normalized;
    }

    private boolean containsSeparators(String input) {
        return input.contains("-") || input.contains(" ");
    }

    private String removeSeparators(String input) {
        return input.replace("-", "").replace(" ", "");
    }

    private Distinguisher getDistinguisher(String input) {
        int separatorIndex = findFirstSeparatorIndex(input);

        String distinguisherCode = input.substring(0, separatorIndex).trim();
        if (!distinguisherCode.matches(DISTINGUISHER_CODE_REGEX))
            throw new InvalidLicencePlateException(String.format("Distinguisher code '%s' does not match pattern", distinguisherCode));

        Optional<Distinguisher> distinguisherOpt = distinguisherRepository.findByCode(distinguisherCode);
        if (distinguisherOpt.isEmpty())
            throw new InvalidLicencePlateException(String.format("No distinguisher found for code %s", distinguisherCode));

        return distinguisherOpt.get();
    }

    private LicencePlate validateAndParseLicencePlate(String input, Distinguisher distinguisher) {
        String normalized = removeSeparators(input);
        if (!normalized.matches(ALPHANUMERIC_ONLY_REGEX))
            throw new InvalidLicencePlateException("Nur Buchstaben A-Z und Ziffern 0-9 erlaubt");

        List<LicencePlate> validParsings = new ArrayList<>();
        if (distinguisher == null) {
            List<Distinguisher> distinguisherCandidates = findDistinguisherCandidates(input);
            if (distinguisherCandidates.isEmpty())
                throw new InvalidLicencePlateException("Unbekanntes Distinguisheral-Kürzel");

            for (Distinguisher distinguisherCandidate : distinguisherCandidates) {
                String remaining = input.substring(distinguisherCandidate.code.length());
                parseRemainingPart(distinguisherCandidate, remaining).ifPresent(validParsings::add);
            }
        } else {
            String remaining = input.substring(distinguisher.code.length());
            parseRemainingPart(distinguisher, remaining).ifPresent(validParsings::add);
        }

        return selectUniqueParsing(validParsings);
    }

    private Optional<LicencePlate> parseRemainingPart(Distinguisher distinguisher, String remainingPart) {
        ModifierExtractionResult modifierResult = extractTrailingModifier(remainingPart.trim());
        String workingString = modifierResult.remainingString();
        String modifier = modifierResult.modifier();
        String cleanedString = removeSeparators(workingString);

        if (Boolean.TRUE.equals(distinguisher.special))
            return specialPlateValidator.validate(distinguisher, cleanedString, modifier);
        return civilianPlateValidator.validate(distinguisher, cleanedString, modifier);
    }

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

    private List<Distinguisher> findDistinguisherCandidates(String input) {
        List<Distinguisher> candidates = new ArrayList<>();
        int maxLength = Math.min(MAX_DISTINGUISHER_CODE_LENGTH, input.length());

        for (int length = 1; length <= maxLength; length++) {
            String potentialCode = input.substring(0, length);
            distinguisherRepository.findByCode(potentialCode).ifPresent(candidates::add);
        }

        return candidates;
    }

    private LicencePlate selectUniqueParsing(List<LicencePlate> parsings) {
        if (parsings.isEmpty())
            throw new InvalidLicencePlateException("Ungültiges Kennzeichen");
        if (parsings.size() > 1)
            throw new AmbiguousLicencePlateException("Kennzeichen mehrdeutig");

        LicencePlate licencePlate = parsings.get(0);

        String distinguisherCode = licencePlate.distinguisher.code;
        String identifier = licencePlate.identifier;
        String combinationKey = distinguisherCode + "-" + identifier;

        if (ForbiddenCombinations.isForbiddenIdentifier(identifier))
            throw new InvalidLicencePlateException(String.format("Illegal identifier '%s' for distinguisher '%s'", identifier, distinguisherCode));
        if (ForbiddenCombinations.isForbiddenPair(combinationKey))
            throw new InvalidLicencePlateException(String.format("Illegal combination %s'", combinationKey));
        return licencePlate;
    }

    private int findFirstSeparatorIndex(String input) {
        int hyphenIndex = input.indexOf('-');
        int spaceIndex = input.indexOf(' ');

        if (hyphenIndex == -1) return spaceIndex;
        if (spaceIndex == -1) return hyphenIndex;

        return Math.min(hyphenIndex, spaceIndex);
    }

    private record ModifierExtractionResult(String remainingString, String modifier) { }
}