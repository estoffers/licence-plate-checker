package tomcom.licenceplatechecker.domain;

public class LicencePlate {

    Region region;
    String identifier;
    String number;
    String modifier;

    private LicencePlate() {
    }

    public LicencePlate(Region region, String identifier, String number, String modifier) {
        this.region = region;
        this.identifier = identifier;
        this.number = number;
        this.modifier = modifier;
    }

    @Override
    public String toString() {
        return region.code + "-" + identifier + number + modifier;
    }
}
