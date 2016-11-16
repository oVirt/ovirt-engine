package org.ovirt.engine.core.utils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.compat.Version;

public class RngUtils {

    /**
     * Checks whether it is ok to have {@code rngDevice} assigned to a VM / Template with {@code effectiveVersion}
     * in {@code cluster}.
     */
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

    @SafeVarargs
    private static <T> boolean containsAtLeastOne(Set<T> collection, T... requiredItems) {
        final Set<T> requiredSet = new HashSet<>(Arrays.asList(requiredItems));
        requiredSet.retainAll(collection);
        return !requiredSet.isEmpty();
    }

    public enum RngValidationResult {
        VALID,
        /** basically invalid, but we treat it as valid to allow users to preview urandom/random change */
        UNSUPPORTED_URANDOM_OR_RANDOM,
        INVALID
    }
}
