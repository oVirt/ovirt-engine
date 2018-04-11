package org.ovirt.engine.core.bll.utils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatedMachineUtils {

    private static final Logger log = LoggerFactory.getLogger(EmulatedMachineUtils.class);

    public static String getEffective(VmBase vmBase, Supplier<Cluster> clusterSupplier) {
        if (vmBase.getCustomEmulatedMachine() != null) {
            return vmBase.getCustomEmulatedMachine();
        }

        // The 'default' to be set
        Cluster cluster = clusterSupplier.get();
        String recentClusterDefault = cluster != null ? cluster.getEmulatedMachine() : "";
        if (vmBase.getCustomCompatibilityVersion() == null) {
            return recentClusterDefault;
        }

        String bestMatch = findBestMatchForEmulatedMachine(
                recentClusterDefault,
                Config.getValue(
                        ConfigValues.ClusterEmulatedMachines,
                        vmBase.getCustomCompatibilityVersion().getValue()));
        log.info("Emulated machine '{}' selected since Custom Compatibility Version is set for '{}'", bestMatch, vmBase);
        return bestMatch;
    }

    protected static String findBestMatchForEmulatedMachine(
            String currentEmulatedMachine,
            List<String> candidateEmulatedMachines) {
        if (candidateEmulatedMachines.contains(currentEmulatedMachine)) {
            return currentEmulatedMachine;
        }
        return candidateEmulatedMachines
                .stream()
                .max(Comparator.comparingInt(s -> StringUtils.indexOfDifference(currentEmulatedMachine, s)))
                .orElse(currentEmulatedMachine);
    }

}
