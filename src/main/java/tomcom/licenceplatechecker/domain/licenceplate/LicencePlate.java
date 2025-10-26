package tomcom.licenceplatechecker.domain.licenceplate;

public class LicencePlate {

    public final Distinguisher distinguisher;
    public final String identifier;
    public final String number;
    public final String modifier; // optional: "H" or "E"

    private LicencePlate(Distinguisher distinguisher, String identifier, String number, String modifier) {
        this.distinguisher = distinguisher;
        this.identifier = identifier == null ? "" : identifier;
        this.number = number == null ? "" : number;
        this.modifier = modifier == null ? "" : modifier;
    }

    public static LicencePlate of(Distinguisher distinguisher, String identifier, String number, String modifier) {
        return new LicencePlate(distinguisher, identifier, number, modifier);
    }

    @Override
    public String toString() {
        if (distinguisher.special != null && distinguisher.special) {
            // Special plates (e.g., Y, THW) usually have no hyphen and no identifier
            return distinguisher.code + number;
        }
        return distinguisher.code + "-" + identifier + number + modifier;
    }
}
