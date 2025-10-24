package tomcom.licenceplatechecker.domain;

public class LicencePlate {

    private final Region region;
    private final String identifier;
    private final String number;
    private final String modifier; // optional: "H" or "E"

    private LicencePlate(Region region, String identifier, String number, String modifier) {
        this.region = region;
        this.identifier = identifier == null ? "" : identifier;
        this.number = number == null ? "" : number;
        this.modifier = modifier == null ? "" : modifier;
    }

    public static LicencePlate of(Region region, String identifier, String number, String modifier) {
        return new LicencePlate(region, identifier, number, modifier);
    }

    public Region getRegion() {
        return region;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getNumber() {
        return number;
    }

    public String getModifier() {
        return modifier;
    }

    @Override
    public String toString() {
        if (region.special != null && region.special) {
            // Special plates (e.g., Y, THW) usually have no hyphen and no identifier
            return region.code + number;
        }
        return region.code + "-" + identifier + number + modifier;
    }
}
