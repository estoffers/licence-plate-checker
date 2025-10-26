package tomcom.licenceplatechecker.application;

import tomcom.licenceplatechecker.domain.licenceplate.LicencePlate;

public class LicencePlatePresenter {
    public static String present(LicencePlate validLicencePlate) {
        return validLicencePlate.toString();
    }
}
