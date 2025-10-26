package tomcom.licenceplatechecker.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tomcom.licenceplatechecker.application.LicencePlatePresenter;
import tomcom.licenceplatechecker.application.dto.ValidationRequest;
import tomcom.licenceplatechecker.domain.licenceplate.LicencePlate;
import tomcom.licenceplatechecker.domain.licenceplate.validator.LicencePlateValidationService;
import tomcom.licenceplatechecker.rest.util.ApiResponse;

@RestController
@RequestMapping("/licence-plate")
class LicencePlateApi {
    private final LicencePlateValidationService licencePlateValidationService;

    LicencePlateApi(LicencePlateValidationService licencePlateValidationService) {
        this.licencePlateValidationService = licencePlateValidationService;
    }

    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> validateLicencePlate(@RequestBody ValidationRequest validationRequest) {
        LicencePlate validLicencePlate = licencePlateValidationService.validateLicencePlate(validationRequest.licencePlate);
        String presentedPlate = LicencePlatePresenter.present(validLicencePlate);
        return ResponseEntity.ok(ApiResponse.success(presentedPlate));
    }
}
