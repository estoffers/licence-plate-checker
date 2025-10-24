package tomcom.licenceplatechecker.domain.validator;

import tomcom.licenceplatechecker.domain.LicencePlate;
import tomcom.licenceplatechecker.domain.Region;

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

    /**
     * Validates and parses a Federal Police licence plate.
     *
     * @param region the region (should be "BP")
     * @param remainingPart the numeric part after the region code
     * @param modifier the modifier (only "E" allowed for electric vehicles 60-61)
     * @return Optional containing the LicencePlate if valid, empty otherwise
     */
    public Optional<LicencePlate> validate(Region region, String remainingPart, String modifier) {
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
            return Optional.of(LicencePlate.of(region, "", remainingPart, ""));
        }

        // Regular BP plates: must be 3-5 digits (2 digits vehicle type + 1-3 digits sequential)
        int length = remainingPart.length();
        if (length < 3 || length > 5) {
            return Optional.empty();
        }

        // Extract vehicle type code (first 2 digits)
        String vehicleTypeCode = remainingPart.substring(0, 2);
        int vehicleType = Integer.parseInt(vehicleTypeCode);

        // Validate vehicle type code
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

        // Validate modifier
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

        return Optional.of(LicencePlate.of(region, "", remainingPart, modifier));
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

    /**
     * Gets the vehicle type range for a given vehicle type code.
     *
     * @param vehicleTypeCode the 2-digit vehicle type code
     * @return the VehicleTypeRange if valid, null otherwise
     */
    private VehicleTypeRange getVehicleTypeRange(int vehicleTypeCode) {
        return VEHICLE_TYPE_RANGES.stream()
            .filter(range -> vehicleTypeCode >= range.min() && vehicleTypeCode <= range.max())
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets the vehicle type for a given licence plate number.
     *
     * @param number the BP plate number (without BP prefix)
     * @return Optional containing the VehicleType if determinable
     */
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

    /**
     * Record representing a vehicle type code range.
     */
    private record VehicleTypeRange(int min, int max, VehicleType type) {}

    /**
     * Enum representing different Federal Police vehicle types.
     */
    public enum VehicleType {
        /**
         * 10-12: Motorcycles
         */
        MOTORCYCLES,

        /**
         * 15-19: Passenger cars (PKW)
         */
        PASSENGER_CARS,

        /**
         * 20-24: Off-road passenger cars
         */
        OFFROAD_PASSENGER_CARS,

        /**
         * 25-29: Light trucks (LKW)
         */
        LIGHT_TRUCKS,

        /**
         * 30-34: Off-road vehicles up to 2t payload, light transporters
         */
        OFFROAD_LIGHT,

        /**
         * 35-39: Trucks up to 6t payload
         */
        TRUCKS_UP_TO_6T,

        /**
         * 40-44: Off-road trucks up to 6t payload
         */
        OFFROAD_TRUCKS_UP_TO_6T,

        /**
         * 45-49: Trucks over 6t payload, heavy transporters, buses (KOM)
         */
        HEAVY_TRUCKS_BUSES,

        /**
         * 50-54: Armored vehicles, protected special vehicles
         */
        ARMORED_VEHICLES,

        /**
         * 55-59: Trailers
         */
        TRAILERS,

        /**
         * 60-61: Electric vehicles (require E modifier)
         */
        ELECTRIC_VEHICLES,

        /**
         * 0600-0699: Test drive plates
         */
        TEST_DRIVE
    }
}