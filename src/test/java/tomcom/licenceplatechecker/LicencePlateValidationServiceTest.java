package tomcom.licenceplatechecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tomcom.licenceplatechecker.domain.licenceplate.exception.AmbiguousLicencePlateException;
import tomcom.licenceplatechecker.domain.licenceplate.exception.InvalidLicencePlateException;
import tomcom.licenceplatechecker.domain.licenceplate.LicencePlate;
import tomcom.licenceplatechecker.domain.licenceplate.Distinguisher;
import tomcom.licenceplatechecker.domain.licenceplate.validator.LicencePlateValidationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(LicencePlateValidationService.class)
@ActiveProfiles("test")
class LicencePlateValidationServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LicencePlateValidationService validationService;

    @BeforeEach
    void setUp() {
        // Load test distinguishers
        createDistinguisher("SG", "Solingen, Stadt", false);
        createDistinguisher("L", "Leipzig", false);
        createDistinguisher("LI", "Lindau (Bodensee)", false);
        createDistinguisher("W", "Wuppertal, Stadt", false);
        createDistinguisher("B", "Berlin", false);
        createDistinguisher("ME", "Mettmann", false);
        createDistinguisher("Y", "Dienstfahrzeuge der Bundeswehr", true);
        createDistinguisher("BN", "Bonn, Stadt", false);

        entityManager.flush();
    }

    private void createDistinguisher(String code, String label, boolean special) {
        Distinguisher distinguisher = new Distinguisher();
        distinguisher.code = code;
        distinguisher.label = label;
        distinguisher.deprecated = false;
        distinguisher.special = special;
        entityManager.persist(distinguisher);
    }

    @Test
    void beispiel1_SGWP100_shouldReturnSG_WP100() {
        // Beispiel 1: Eingabe SGWP100, Ausgabe SG-WP100
        LicencePlate result = validationService.validateLicencePlate("W-SE515");

        assertThat(result.distinguisher.code).isEqualTo("W");
        assertThat(result.identifier).isEqualTo("SE");
        assertThat(result.number).isEqualTo("515");
        assertThat(result.toString()).isEqualTo("W-SE515");
    }

    @Test
    void beispiel2_LIT433_shouldThrowAmbiguousException() {
        // Beispiel 2: Eingabe LIT433, Ausgabe Fehler, weil nicht eindeutig, könnte L-IT433 oder LI-T433 sein
        assertThatThrownBy(() -> validationService.validateLicencePlate("LIT433"))
            .isInstanceOf(AmbiguousLicencePlateException.class)
            .hasMessageContaining("mehrdeutig");
    }

    @Test
    void beispiel3_W_SS88_shouldThrowInvalidException_Sittenwidrig() {
        // Beispiel 3: Eingabe W-SS88, Ausgabe Fehler, weil sittenwidrig
        assertThatThrownBy(() -> validationService.validateLicencePlate("W-SS88"))
            .isInstanceOf(InvalidLicencePlateException.class);
    }

    @Test
    void beispiel4_B_XY700_shouldReturnB_XY700() {
        // Beispiel 4: Eingabe B-XY700, Ausgabe B-XY700
        LicencePlate result = validationService.validateLicencePlate("B-XY700");

        assertThat(result.distinguisher.code).isEqualTo("B");
        assertThat(result.identifier).isEqualTo("XY");
        assertThat(result.number).isEqualTo("700");
        assertThat(result.toString()).isEqualTo("B-XY700");
    }

    @Test
    void beispiel5_ME_AB_3333_shouldReturnME_AB3333() {
        // Beispiel 5: Eingabe ME AB 3333, Ausgabe ME-AB3333
        LicencePlate result = validationService.validateLicencePlate("ME AB 3333");

        assertThat(result.distinguisher.code).isEqualTo("ME");
        assertThat(result.identifier).isEqualTo("AB");
        assertThat(result.number).isEqualTo("3333");
        assertThat(result.toString()).isEqualTo("ME-AB3333");
    }

    @Test
    void beispiel6_B_NN_1234_shouldReturnB_NN1234() {
        // Beispiel 6: Eingabe B-NN 1234, Ausgabe B-NN1234
        LicencePlate result = validationService.validateLicencePlate("B-NN 1234");

        assertThat(result.distinguisher.code).isEqualTo("B");
        assertThat(result.identifier).isEqualTo("NN");
        assertThat(result.number).isEqualTo("1234");
        assertThat(result.toString()).isEqualTo("B-NN1234");
    }

    @Test
    void beispiel7_BNN1234_shouldThrowAmbiguousException() {
        // Beispiel 7: Eingabe BNN1234, Ausgabe Fehler weil nicht eindeutig
        assertThatThrownBy(() -> validationService.validateLicencePlate("BNN1234"))
            .isInstanceOf(AmbiguousLicencePlateException.class)
            .hasMessageContaining("mehrdeutig");
    }

    @Test
    void beispiel8_Y123456_shouldReturnY123456_Special() {
        // Beispiel 8: Eingabe Y123456, Ausgabe Y123456
        LicencePlate result = validationService.validateLicencePlate("Y123456");

        assertThat(result.distinguisher.code).isEqualTo("Y");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("123456");
        assertThat(result.toString()).isEqualTo("Y123456");
    }

    // Red Plate Tests (Dealer plates - 06)
    @Test
    void dealerPlate_B06123_shouldReturnB_06123() {
        // Dealer plate (Händlerkennzeichen): no identifier, starts with 06
        LicencePlate result = validationService.validateLicencePlate("B06123");

        assertThat(result.distinguisher.code).isEqualTo("B");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("06123");
        assertThat(result.toString()).isEqualTo("B-06123");
    }

    @Test
    void dealerPlate_B_06123_withSeparator_shouldReturnB_06123() {
        // Dealer plate with separator
        LicencePlate result = validationService.validateLicencePlate("B-06123");

        assertThat(result.distinguisher.code).isEqualTo("B");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("06123");
        assertThat(result.toString()).isEqualTo("B-06123");
    }

    @Test
    void dealerPlate_ME_061234_shouldReturnME_061234() {
        // Dealer plate with max 6 digits
        LicencePlate result = validationService.validateLicencePlate("ME-061234");

        assertThat(result.distinguisher.code).isEqualTo("ME");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("061234");
        assertThat(result.toString()).isEqualTo("ME-061234");
    }

    @Test
    void dealerPlate_B_0612345_tooLong_shouldThrowInvalidException() {
        // Dealer plate with 7 digits exceeds max length
        assertThatThrownBy(() -> validationService.validateLicencePlate("B-0612345"))
            .isInstanceOf(InvalidLicencePlateException.class);
    }

    @Test
    void dealerPlate_withModifier_shouldThrowInvalidException() {
        // Dealer plates do not allow modifiers
        assertThatThrownBy(() -> validationService.validateLicencePlate("B-061234H"))
            .isInstanceOf(InvalidLicencePlateException.class);
    }

    // Oldtimer Red Plate Tests (07)
    @Test
    void oldtimerPlate_B07456_shouldReturnB_07456() {
        // Oldtimer red plate: starts with 07
        LicencePlate result = validationService.validateLicencePlate("B07456");

        assertThat(result.distinguisher.code).isEqualTo("B");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("07456");
        assertThat(result.toString()).isEqualTo("B-07456");
    }

    @Test
    void oldtimerPlate_B_07456_withSeparator_shouldReturnB_07456() {
        // Oldtimer red plate with separator
        LicencePlate result = validationService.validateLicencePlate("B-07456");

        assertThat(result.distinguisher.code).isEqualTo("B");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("07456");
        assertThat(result.toString()).isEqualTo("B-07456");
    }

    @Test
    void oldtimerPlate_withModifier_shouldThrowInvalidException() {
        // Red plates (including oldtimer) do not allow modifiers
        assertThatThrownBy(() -> validationService.validateLicencePlate("ME-071234H"))
            .isInstanceOf(InvalidLicencePlateException.class);
    }

    // Technical Inspection Red Plate Tests (05)
    @Test
    void technicalInspectionPlate_B05789_shouldReturnB_05789() {
        // Technical inspection plate: starts with 05
        LicencePlate result = validationService.validateLicencePlate("B05789");

        assertThat(result.distinguisher.code).isEqualTo("B");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("05789");
        assertThat(result.toString()).isEqualTo("B-05789");
    }

    @Test
    void technicalInspectionPlate_W_051234_withSeparator_shouldReturnW_051234() {
        // Technical inspection plate with separator
        LicencePlate result = validationService.validateLicencePlate("W-051234");

        assertThat(result.distinguisher.code).isEqualTo("W");
        assertThat(result.identifier).isEmpty();
        assertThat(result.number).isEqualTo("051234");
        assertThat(result.toString()).isEqualTo("W-051234");
    }

    @Test
    void technicalInspectionPlate_withModifier_shouldThrowInvalidException() {
        // Red plates do not allow modifiers
        assertThatThrownBy(() -> validationService.validateLicencePlate("W-051234E"))
            .isInstanceOf(InvalidLicencePlateException.class);
    }

    // Invalid Red Plate Tests
    @Test
    void invalidRedPlate_B08123_shouldThrowInvalidException() {
        // Number starting with 08 (not 05, 06, or 07) without identifier should be invalid
        assertThatThrownBy(() -> validationService.validateLicencePlate("B08123"))
            .isInstanceOf(InvalidLicencePlateException.class);
    }

    @Test
    void invalidRedPlate_B04123_shouldThrowInvalidException() {
        // Number starting with 04 without identifier should be invalid
        assertThatThrownBy(() -> validationService.validateLicencePlate("B04123"))
            .isInstanceOf(InvalidLicencePlateException.class);
    }
}
