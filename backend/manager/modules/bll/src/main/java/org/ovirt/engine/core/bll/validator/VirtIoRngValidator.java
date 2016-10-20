package org.ovirt.engine.core.bll.validator;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;
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
     * @param effectiveVersion of vm-like entity the rng device will be assigned to
     */
    public ValidationResult canAddRngDevice(Cluster cluster, VmRngDevice rngDevice, Version effectiveVersion) {
        final RngValidationResult rngValidationResult = validate(cluster, rngDevice, effectiveVersion);
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

    @SafeVarargs
    private static <T> boolean containsAtLeastOne(Set<T> collection, T... requiredItems) {
        final Set<T> requiredSet = new HashSet<>(Arrays.asList(requiredItems));
        requiredSet.retainAll(collection);
        return !requiredSet.isEmpty();
    }

    public static RngValidationResult validate(Cluster cluster, VmRngDevice rngDevice, Version effectiveVersion) {
        VmRngDevice.Source source = rngDevice.getSource();

        // This can be dropped when we stop to support 4.0 compatibility level
        if (cluster.getCompatibilityVersion() != null
                && !cluster.getCompatibilityVersion().equals(effectiveVersion)
                && EnumSet.of(VmRngDevice.Source.URANDOM, VmRngDevice.Source.RANDOM).contains(source)
                && containsAtLeastOne(cluster.getRequiredRngSources(), VmRngDevice.Source.URANDOM, VmRngDevice.Source.RANDOM)
                && !cluster.getRequiredRngSources().contains(source)) {
            return RngValidationResult.UNSUPPORTED_URANDOM_OR_RANDOM;
        }

        final boolean valid = cluster.getRequiredRngSources().contains(source);
        return valid ? RngValidationResult.VALID : RngValidationResult.INVALID;
    }

    public enum RngValidationResult {
        VALID,
        /** basically invalid, but we treat it as valid to allow users to preview urandom/random change */
        UNSUPPORTED_URANDOM_OR_RANDOM,
        INVALID
    }
}
