package org.ovirt.engine.core.bll.network.host;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class HostValidator {

    public static final String VAR_HOST_STATUS = "hostStatus";
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
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                ReplacementUtils.replaceWith(VAR_HOST_STATUS, supportedStatuses, ",", supportedStatuses.size()));
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
