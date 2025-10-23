package tomcom.licenceplatechecker.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import tomcom.licenceplatechecker.domain.LicencePlateValidationService;

@RestController
@RequestMapping("/licence-plate")
class LicencePlateApi {
    private final LicencePlateValidationService licencePlateValidationService;

    LicencePlateApi(LicencePlateValidationService licencePlateValidationService) {
        this.licencePlateValidationService = licencePlateValidationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateLicencePlate(@RequestBody String licencePlate) {
        boolean isValid = licencePlateValidationService.validateLicencePlate(licencePlate);
        if (!isValid)
            return ResponseEntity.badRequest().body("Invalid licence plate");
        return ResponseEntity.ok(licencePlate);
    }

}
