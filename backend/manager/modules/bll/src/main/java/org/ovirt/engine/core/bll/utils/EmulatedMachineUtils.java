package org.ovirt.engine.core.bll.utils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.EmulatedMachineCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatedMachineUtils {

    private static final Logger log = LoggerFactory.getLogger(EmulatedMachineUtils.class);

    /**
     * Get effective emulated machine type.
     *
     * @param vmBase - VM entity to check for
     * @param clusterSupplier - Supplier of non-null Cluster
     * @return The effective emulated machine type.
     */
    public static String getEffective(VmBase vmBase, Supplier<Cluster> clusterSupplier) {
        if (vmBase.getCustomEmulatedMachine() != null) {
            return vmBase.getCustomEmulatedMachine();
        }

        // The 'default' to be set
        Cluster cluster = clusterSupplier.get();
        String recentClusterDefault =
                ClusterEmulatedMachines.forChipset(cluster.getEmulatedMachine(), vmBase.getEffectiveBiosType().getChipsetType());
        if (vmBase.getCustomCompatibilityVersion() == null) {
            return recentClusterDefault;
        }

        String bestMatch = findBestMatchForEmulatedMachine(
                vmBase.getEffectiveBiosType().getChipsetType(),
                recentClusterDefault,
                Config.getValue(
                        ConfigValues.ClusterEmulatedMachines,
                        CompatibilityVersionUtils.getEffective(vmBase, cluster).getValue()));
        log.info("Emulated machine '{}' which is different than that of the cluster is set for '{}'({})",
                bestMatch, vmBase.getName(), vmBase.getId());
        return bestMatch;
    }

    protected static String findBestMatchForEmulatedMachine(
            ChipsetType chipsetType,
            String currentEmulatedMachine,
            List<String> candidateEmulatedMachines) {
        return candidateEmulatedMachines
                .stream()
                .filter(EmulatedMachineCommonUtils.chipsetMatches(chipsetType))
                .max(Comparator.comparingInt(s -> {
                    int index = StringUtils.indexOfDifference(currentEmulatedMachine, s);
                    return index < 0 ? Integer.MAX_VALUE : index;
                }))
                .orElse(currentEmulatedMachine);
    }

}
