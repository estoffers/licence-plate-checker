package tomcom.licenceplatechecker.application;

import tomcom.licenceplatechecker.domain.LicencePlate;

public class LicencePlatePresenter {
    public String present(LicencePlate validLicencePlate) {
        return validLicencePlate.toString();
    }
}
