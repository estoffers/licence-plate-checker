package tomcom.licenceplatechecker.domain.licenceplate.validator;

import tomcom.licenceplatechecker.domain.licenceplate.Distinguisher;
import tomcom.licenceplatechecker.domain.licenceplate.LicencePlate;

import java.util.Optional;
import java.util.Set;

/**
 * Validator for German Federal Police (BP - Bundespolizei) licence plates.
 * <p>
 * Federal Police plates consist of BP followed by two digit groups:
 * <ul>
 *   <li>First group (2 digits): Vehicle type code</li>
 *   <li>Second group (1-3 digits): Sequential number</li>
 * </ul>
 * </p>
 * <p>
 * Valid vehicle type codes (first group):
 * <ul>
 *   <li>10-12: Motorcycles</li>
 *   <li>15-19: Passenger cars (PKW)</li>
 *   <li>20-24: Off-road passenger cars</li>
 *   <li>25-29: Light trucks (LKW)</li>
 *   <li>30-34: Off-road vehicles up to 2t payload, light transporters</li>
 *   <li>35-39: Trucks up to 6t payload</li>
 *   <li>40-44: Off-road trucks up to 6t payload</li>
 *   <li>45-49: Trucks over 6t payload, heavy transporters, buses</li>
 *   <li>50-54: Armored vehicles, protected special vehicles</li>
 *   <li>55-59: Trailers</li>
 *   <li>60-61: Electric vehicles (with E modifier at the end)</li>
 * </ul>
 * </p>
 * <p>
 * Special: BP-0600 to BP-0699 are red test drive plates
 * </p>
 * <p>
 * Valid examples: BP151, BP1599, BP301, BP6012E<br>
 * Invalid examples: BP09123 (invalid vehicle code), BP153456 (sequential number too long), BP151H (invalid modifier)
 * </p>
 */
public class FederalPoliceValidator {

    private static final int RED_PLATE_MIN = 600;
    private static final int RED_PLATE_MAX = 699;

    private static final int MIN_SEQUENTIAL_LENGTH = 1;
    private static final int MAX_SEQUENTIAL_LENGTH = 3;

    // Valid vehicle type code ranges
    private static final Set<VehicleTypeRange> VEHICLE_TYPE_RANGES = Set.of(
        new VehicleTypeRange(10, 12, VehicleType.MOTORCYCLES),
        new VehicleTypeRange(15, 19, VehicleType.PASSENGER_CARS),
        new VehicleTypeRange(20, 24, VehicleType.OFFROAD_PASSENGER_CARS),
        new VehicleTypeRange(25, 29, VehicleType.LIGHT_TRUCKS),
        new VehicleTypeRange(30, 34, VehicleType.OFFROAD_LIGHT),
        new VehicleTypeRange(35, 39, VehicleType.TRUCKS_UP_TO_6T),
        new VehicleTypeRange(40, 44, VehicleType.OFFROAD_TRUCKS_UP_TO_6T),
        new VehicleTypeRange(45, 49, VehicleType.HEAVY_TRUCKS_BUSES),
        new VehicleTypeRange(50, 54, VehicleType.ARMORED_VEHICLES),
        new VehicleTypeRange(55, 59, VehicleType.TRAILERS),
        new VehicleTypeRange(60, 61, VehicleType.ELECTRIC_VEHICLES)
    );

    public Optional<LicencePlate> validate(Distinguisher distinguisher, String remainingPart, String modifier) {
        // Must be only digits
        if (!remainingPart.matches("[0-9]+")) {
            return Optional.empty();
        }

        // Check if it's a red test drive plate (BP-0600 to BP-0699)
        if (isRedPlate(remainingPart)) {
            // Red plates do not allow modifiers
            if (!modifier.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(LicencePlate.of(distinguisher, "", remainingPart, ""));
        }

        // Regular BP plates: must be 3-5 digits (2 digits vehicle type + 1-3 digits sequential)
        int length = remainingPart.length();
        if (length < 3 || length > 5) {
            return Optional.empty();
        }

        String vehicleTypeCode = remainingPart.substring(0, 2);
        int vehicleType = Integer.parseInt(vehicleTypeCode);

        VehicleTypeRange vehicleTypeRange = getVehicleTypeRange(vehicleType);
        if (vehicleTypeRange == null) {
            return Optional.empty();
        }

        // Extract sequential number (remaining 1-3 digits)
        String sequentialNumber = remainingPart.substring(2);
        if (sequentialNumber.length() < MIN_SEQUENTIAL_LENGTH ||
            sequentialNumber.length() > MAX_SEQUENTIAL_LENGTH) {
            return Optional.empty();
        }

        if (!modifier.isEmpty()) {
            // Only electric vehicles (60-61) can have E modifier
            if (vehicleTypeRange.type() != VehicleType.ELECTRIC_VEHICLES || !modifier.equals("E")) {
                return Optional.empty();
            }
        } else {
            // Electric vehicles must have E modifier
            if (vehicleTypeRange.type() == VehicleType.ELECTRIC_VEHICLES) {
                return Optional.empty();
            }
        }

        return Optional.of(LicencePlate.of(distinguisher, "", remainingPart, modifier));
    }

    /**
     * Checks if the number is a BP red test drive plate.
     * BP has special red plates in the range 0600-0699 for test drives.
     *
     * @param number the number to check
     * @return true if it's a valid BP red plate number
     */
    private boolean isRedPlate(String number) {
        if (!number.matches("[0-9]{4}")) {
            return false;
        }

        int value = Integer.parseInt(number);
        return value >= RED_PLATE_MIN && value <= RED_PLATE_MAX;
    }

    private VehicleTypeRange getVehicleTypeRange(int vehicleTypeCode) {
        return VEHICLE_TYPE_RANGES.stream()
            .filter(range -> vehicleTypeCode >= range.min() && vehicleTypeCode <= range.max())
            .findFirst()
            .orElse(null);
    }

    public Optional<VehicleType> getVehicleType(String number) {
        if (number == null || number.length() < 2) {
            return Optional.empty();
        }

        // Check if it's a red plate
        if (isRedPlate(number)) {
            return Optional.of(VehicleType.TEST_DRIVE);
        }

        // Extract vehicle type code
        String vehicleTypeCode = number.substring(0, 2);
        int code = Integer.parseInt(vehicleTypeCode);

        VehicleTypeRange range = getVehicleTypeRange(code);
        return range != null ? Optional.of(range.type()) : Optional.empty();
    }
    private record VehicleTypeRange(int min, int max, VehicleType type) {}

    public enum VehicleType {
        MOTORCYCLES,
        PASSENGER_CARS,
        OFFROAD_PASSENGER_CARS,
        LIGHT_TRUCKS,
        OFFROAD_LIGHT,
        TRUCKS_UP_TO_6T,
        OFFROAD_TRUCKS_UP_TO_6T,
        HEAVY_TRUCKS_BUSES,
        ARMORED_VEHICLES,
        TRAILERS,
        ELECTRIC_VEHICLES,
        TEST_DRIVE
    }
}