package tomcom.licenceplatechecker.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tomcom.licenceplatechecker.application.LicencePlatePresenter;
import tomcom.licenceplatechecker.domain.LicencePlate;
import tomcom.licenceplatechecker.domain.LicencePlateValidationService;

@RestController
@RequestMapping("/licence-plate")
class LicencePlateApi {
    private final LicencePlateValidationService licencePlateValidationService;

    LicencePlateApi(LicencePlateValidationService licencePlateValidationService) {
        this.licencePlateValidationService = licencePlateValidationService;
    }

    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> validateLicencePlate(@RequestBody ValidationRequest validationRequest) {
        LicencePlate validLicencePlate = licencePlateValidationService.validateLicencePlate(validationRequest.licencePlate);
        return ResponseEntity.ok(new LicencePlatePresenter().present(validLicencePlate));
    }
}
