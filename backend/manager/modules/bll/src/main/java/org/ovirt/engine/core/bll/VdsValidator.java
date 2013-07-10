package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

/**
 * Contains methods used to validate VDS status to execute some operation on it
 */
public class VdsValidator {
    /**
     * Vds instance
     */
    private VDS vds;

    /**
     * Creates an instance with specified VDS
     *
     * @param vds
     *            specified VDS
     */
    public VdsValidator(VDS vds) {
        this.vds = vds;
    }

    /**
     * Determines if the VDS status is legal for execute fencing on host (either SSH Soft Fencing or real one)
     *
     * @return {@code true}, if fencing should be executed, otherwise {@code false}
     */
    public boolean shouldVdsBeFenced() {
        boolean result = false;
        // Not using exists() here in order not to add canDoAction message
        if (vds == null) {
            return false;
        }

        switch (vds.getStatus()) {
        case Down:
        case InstallFailed:
        case Maintenance:
        case NonOperational:
        case NonResponsive:
            result = true;
            break;

        default:
            break;
        }

        return result;
    }

    public ValidationResult exists() {
        if (vds == null) {
            return new ValidationResult(VdcBllMessages.VDS_DOES_NOT_EXIST);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateStatusForActivation() {
        ValidationResult existsValidation = exists();
        if (!existsValidation.isValid()) {
            return existsValidation;
        }
        if (VDSStatus.Up == vds.getStatus()) {
            return new ValidationResult(VdcBllMessages.VDS_ALREADY_UP);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateUniqueId() {
        if (StringUtils.isBlank(vds.getUniqueId()) && Config.<Boolean> GetValue(ConfigValues.InstallVds)) {
            return new ValidationResult(VdcBllMessages.VDS_NO_UUID);
        }
        return ValidationResult.VALID;
    }
}
