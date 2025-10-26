package tomcom.licenceplatechecker.domain.licenceplate.validator;

import java.util.Set;

public final class ForbiddenCombinations {
    private ForbiddenCombinations() {}

    static final Set<String> FORBIDDEN_IDENTIFIERS = Set.of("HJ", "KZ", "NS", "SA", "SS");
    static final Set<String> FORBIDDEN_PAIRS = Set.of(
        "D-IS", "SU-IS", "MR-IS", "DA-IS",
        "S-A", "S-S", "S-D", "K-Z", "S-ED",
        "N-PD", "N-SU", "N-S",
        "WAF-FE", "SK-IN", "IZ-AN", "HEI-L",
        "SU-FF", "R-NS", "BUL-LE", "MO-RD"
    );

    public static boolean isForbiddenIdentifier(String identifier) {
        if (identifier == null) return false;
        return FORBIDDEN_IDENTIFIERS.contains(identifier);
    }

    public static boolean isForbiddenPair(String pair) {
        if (pair == null) return false;
        return FORBIDDEN_PAIRS.contains(pair);
    }
}
