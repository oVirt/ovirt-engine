package org.ovirt.engine.core.bll.validator;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpgradeHostValidator extends HostValidator {

    public UpgradeHostValidator(DbFacade dbFacade, VDS host) {
        super(dbFacade, host);
    }

    public ValidationResult hostExists() {
        return ValidationResult.failWith(VdcBllMessages.VDS_INVALID_SERVER_ID).when(getHost() == null);
    }

    public ValidationResult statusSupportedForHostUpgrade() {
        return ValidationResult.failWith(VdcBllMessages.VDS_CANNOT_INSTALL_STATUS_ILLEGAL)
                .when(getHost().getStatus() != VDSStatus.Maintenance
                        && getHost().getStatus() != VDSStatus.InstallFailed
                        && getHost().getStatus() != VDSStatus.Up);
    }

    public ValidationResult statusSupportedForHostUpgradeInternal() {
        return ValidationResult.failWith(VdcBllMessages.VDS_CANNOT_INSTALL_STATUS_ILLEGAL)
                .when(getHost().getStatus() != VDSStatus.Maintenance);
    }

    public ValidationResult updatesAvailable() {
        return ValidationResult.failWith(VdcBllMessages.NO_AVAILABLE_UPDATES_FOR_HOST)
                .unless(getHost().getVdsType() == VDSType.oVirtNode || getHost().isUpdateAvailable());
    }

    public ValidationResult imageProvidedForOvirtNode(String image) {
        return ValidationResult.failWith(VdcBllMessages.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE)
                .when(getHost().getVdsType() == VDSType.oVirtNode && StringUtils.isBlank(image));
    }
}
