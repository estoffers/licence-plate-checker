package tomcom.licenceplatechecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import tomcom.licenceplatechecker.domain.LicencePlate;
import tomcom.licenceplatechecker.domain.Region;
import tomcom.licenceplatechecker.domain.validator.FederalPoliceValidator;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FederalPoliceValidatorTest {

    private FederalPoliceValidator validator;
    private Region testRegion;

    @BeforeEach
    void setUp() {
        validator = new FederalPoliceValidator();
        testRegion = new Region();
        testRegion.code = "BP";
        testRegion.label = "Bundespolizei";
        testRegion.special = true;
    }

    // Motorcycles (10-12)
    @ParameterizedTest
    @ValueSource(strings = {"101", "1099", "119", "12999"})
    void validate_motorcycles_shouldReturnLicencePlate(String number) {
        Optional<LicencePlate> result = validator.validate(testRegion, number, "");
        
        assertThat(result).isPresent();
        assertThat(result.get().getNumber()).isEqualTo(number);
        assertThat(validator.getVehicleType(number))
            .contains(FederalPoliceValidator.VehicleType.MOTORCYCLES);
    }

    // Passenger Cars (15-19)
    @ParameterizedTest
    @ValueSource(strings = {"151", "1599", "191", "19456"})
    void validate_passengerCars_shouldReturnLicencePlate(String number) {
        Optional<LicencePlate> result = validator.validate(testRegion, number, "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType(number))
            .contains(FederalPoliceValidator.VehicleType.PASSENGER_CARS);
    }

    // Off-road Passenger Cars (20-24)
    @Test
    void validate_offroadPassengerCars_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "201", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("201"))
            .contains(FederalPoliceValidator.VehicleType.OFFROAD_PASSENGER_CARS);
    }

    // Light Trucks (25-29)
    @Test
    void validate_lightTrucks_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "251", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("251"))
            .contains(FederalPoliceValidator.VehicleType.LIGHT_TRUCKS);
    }

    // Off-road Light (30-34)
    @Test
    void validate_offroadLight_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "301", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("301"))
            .contains(FederalPoliceValidator.VehicleType.OFFROAD_LIGHT);
    }

    // Trucks up to 6t (35-39)
    @Test
    void validate_trucksUpTo6t_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "351", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("351"))
            .contains(FederalPoliceValidator.VehicleType.TRUCKS_UP_TO_6T);
    }

    // Off-road Trucks up to 6t (40-44)
    @Test
    void validate_offroadTrucksUpTo6t_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "401", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("401"))
            .contains(FederalPoliceValidator.VehicleType.OFFROAD_TRUCKS_UP_TO_6T);
    }

    // Heavy Trucks and Buses (45-49)
    @Test
    void validate_heavyTrucksBuses_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "451", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("451"))
            .contains(FederalPoliceValidator.VehicleType.HEAVY_TRUCKS_BUSES);
    }

    // Armored Vehicles (50-54)
    @Test
    void validate_armoredVehicles_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "501", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("501"))
            .contains(FederalPoliceValidator.VehicleType.ARMORED_VEHICLES);
    }

    // Trailers (55-59)
    @Test
    void validate_trailers_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "551", "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType("551"))
            .contains(FederalPoliceValidator.VehicleType.TRAILERS);
    }

    // Electric Vehicles (60-61) - Must have E modifier
    @Test
    void validate_electricVehicles_withEModifier_shouldReturnLicencePlate() {
        Optional<LicencePlate> result = validator.validate(testRegion, "601", "E");
        
        assertThat(result).isPresent();
        assertThat(result.get().getModifier()).isEqualTo("E");
        assertThat(validator.getVehicleType("601"))
            .contains(FederalPoliceValidator.VehicleType.ELECTRIC_VEHICLES);
    }

    @Test
    void validate_electricVehicles_withoutModifier_shouldReturnEmpty() {
        // Electric vehicles must have E modifier
        Optional<LicencePlate> result = validator.validate(testRegion, "601", "");
        
        assertThat(result).isEmpty();
    }

    @Test
    void validate_electricVehicles_withHModifier_shouldReturnEmpty() {
        // Electric vehicles can only have E modifier, not H
        Optional<LicencePlate> result = validator.validate(testRegion, "601", "H");
        
        assertThat(result).isEmpty();
    }

    // Red Test Drive Plates (0600-0699)
    @ParameterizedTest
    @ValueSource(strings = {"0600", "0650", "0699"})
    void validate_redPlates_shouldReturnLicencePlate(String number) {
        Optional<LicencePlate> result = validator.validate(testRegion, number, "");
        
        assertThat(result).isPresent();
        assertThat(validator.getVehicleType(number))
            .contains(FederalPoliceValidator.VehicleType.TEST_DRIVE);
    }

    @Test
    void validate_redPlate_withModifier_shouldReturnEmpty() {
        // Red plates do not allow modifiers
        Optional<LicencePlate> result = validator.validate(testRegion, "0650", "E");
        
        assertThat(result).isEmpty();
    }

    // Invalid Cases
    @Test
    void validate_invalidVehicleTypeCode_shouldReturnEmpty() {
        // 09 is not a valid vehicle type code
        Optional<LicencePlate> result = validator.validate(testRegion, "091", "");
        
        assertThat(result).isEmpty();
    }

    @Test
    void validate_tooShort_shouldReturnEmpty() {
        // Must be at least 3 digits (2 vehicle type + 1 sequential)
        Optional<LicencePlate> result = validator.validate(testRegion, "15", "");
        
        assertThat(result).isEmpty();
    }

    @Test
    void validate_sequentialTooLong_shouldReturnEmpty() {
        // Sequential number max 3 digits
        Optional<LicencePlate> result = validator.validate(testRegion, "151234", "");
        
        assertThat(result).isEmpty();
    }

    @Test
    void validate_nonElectricWithEModifier_shouldReturnEmpty() {
        // Non-electric vehicles cannot have E modifier
        Optional<LicencePlate> result = validator.validate(testRegion, "151", "E");
        
        assertThat(result).isEmpty();
    }

    @Test
    void validate_withLetters_shouldReturnEmpty() {
        Optional<LicencePlate> result = validator.validate(testRegion, "15AB1", "");
        
        assertThat(result).isEmpty();
    }

    @Test
    void validate_invalidVehicleTypeRange_shouldReturnEmpty() {
        // 14 is between valid ranges (10-12 and 15-19)
        Optional<LicencePlate> result = validator.validate(testRegion, "141", "");
        
        assertThat(result).isEmpty();
    }
}
