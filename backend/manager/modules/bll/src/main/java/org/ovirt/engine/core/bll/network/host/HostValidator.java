package org.ovirt.engine.core.bll.network.host;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class HostValidator {

    private final VDS host;
    private final boolean internalExecution;

    public HostValidator(VDS host, boolean internalExecution) {
        this.host = host;
        this.internalExecution = internalExecution;
    }


    private ValidationResult hostExist() {
        if (host == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
        }

        return ValidationResult.VALID;
    }

    private ValidationResult hostStatusLegalForSetupNetworks() {
        List<VDSStatus> supportedStatuses =
                Arrays.asList(VDSStatus.Maintenance, VDSStatus.Up, VDSStatus.NonOperational);

        boolean hostStatusLegalForSetupNetworks = supportedStatuses.contains(host.getStatus())
                || host.getStatus() == VDSStatus.Installing && internalExecution;

        if (!hostStatusLegalForSetupNetworks) {
            //            violations.addViolation(EngineMessage.VAR__HOST_STATUS__UP_MAINTENANCE_OR_NON_OPERATIONAL, host.getName());
            //            violations.addViolation(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL, host.getName());

            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                host.getName(),
                host.getStatus().name()); //TODO MM: fix message & replacements.
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validate() {
        ValidationResult vr = ValidationResult.VALID;

        vr = skipValidation(vr) ? vr : hostExist();
        vr = skipValidation(vr) ? vr : hostStatusLegalForSetupNetworks();

        return vr;
    }

    private boolean skipValidation(ValidationResult validationResult) {
        return !validationResult.isValid();
    }
}
