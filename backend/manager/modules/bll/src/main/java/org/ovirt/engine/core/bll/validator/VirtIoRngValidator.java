package org.ovirt.engine.core.bll.validator;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.RngUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtIoRngValidator {

    private final Logger log = LoggerFactory.getLogger(VirtIoRngValidator.class);

    /**
     * If {@code cluster} compatibility level matches {@code effectiveVersion} or {@code effectiveVersion} is null, it
     * checks whether the rng device source is among cluster required sources.
     *
     * <p>Otherwise {@link VmRngDevice.Source.URANDOM} and {@link VmRngDevice.Source.RANDOM} rng sources are considered
     * valid even if not present in {@link Cluster#getRequiredRngSources()}. This exception allows to change rng source
     * according to custom compatibility level. Warning is printed in this case.</p>
     *
     */
    public ValidationResult canAddRngDevice(Cluster cluster, VmRngDevice rngDevice) {
        final RngUtils.RngValidationResult rngValidationResult = RngUtils.validate(cluster, rngDevice);
        switch (rngValidationResult) {
            case VALID:
                return ValidationResult.VALID;
            case UNSUPPORTED_URANDOM_OR_RANDOM:
                log.warn("Random number source {} is not supported in cluster '{}' compatibility version {}.",
                        rngDevice.getSource(),
                        cluster.getName(),
                        cluster.getCompatibilityVersion());
                return ValidationResult.VALID;
            case INVALID:
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GENERATOR_NOT_SUPPORTED_BY_CLUSTER);
            default:
                throw new RuntimeException("Unknown enum constant " + rngValidationResult);
        }
    }
}
