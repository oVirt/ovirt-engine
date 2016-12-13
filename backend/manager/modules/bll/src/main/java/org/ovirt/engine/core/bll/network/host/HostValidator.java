package org.ovirt.engine.core.bll.network.host;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostValidator {

    private static final Logger logger = LoggerFactory.getLogger(HostValidator.class);
    private static final List<VDSStatus> LEGAL_STATUSES =
            Arrays.asList(VDSStatus.Maintenance, VDSStatus.Up, VDSStatus.NonOperational);
    private static final String LEGAL_STATUSES_STR =
            LEGAL_STATUSES.stream().map(VDSStatus::name).collect(Collectors.joining(", "));

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

        VDSStatus hostStatus = host.getStatus();
        boolean hostStatusLegalForSetupNetworks = LEGAL_STATUSES.contains(hostStatus)
                || hostStatus == VDSStatus.Installing && internalExecution;

        if (!hostStatusLegalForSetupNetworks) {
            logger.error(
                    "Unable to setup network: operation can only be done when Host status is one of: {};" +
                            " current status is {}",
                    LEGAL_STATUSES_STR,
                    hostStatus);
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL,
                    ReplacementUtils.replaceWith(VAR_HOST_STATUS, LEGAL_STATUSES, ",", LEGAL_STATUSES.size()));
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
