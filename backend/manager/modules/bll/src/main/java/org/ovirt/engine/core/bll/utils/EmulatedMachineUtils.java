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
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatedMachineUtils {

    private static final Logger log = LoggerFactory.getLogger(EmulatedMachineUtils.class);

    private static final String I440FX_CHIPSET_NAME = ChipsetType.I440FX.getChipsetName();
    private static final String Q35_CHIPSET_NAME = ChipsetType.Q35.getChipsetName();

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
        String recentClusterDefault = cluster.getEmulatedMachine();
        if (vmBase.getCustomCompatibilityVersion() == null
                && chipsetMatchesEmulatedMachine(vmBase.getBiosType().getChipsetType(), recentClusterDefault)) {
            return recentClusterDefault;
        }

        String bestMatch = findBestMatchForEmulatedMachine(
                vmBase.getBiosType().getChipsetType(),
                recentClusterDefault,
                Config.getValue(
                        ConfigValues.ClusterEmulatedMachines,
                        CompatibilityVersionUtils.getEffective(vmBase, cluster).getValue()));
        log.info("Emulated machine '{}' selected since Custom Compatibility Version is set for '{}'", bestMatch, vmBase);
        return bestMatch;
    }

    protected static String findBestMatchForEmulatedMachine(
            ChipsetType chipsetType,
            String currentEmulatedMachine,
            List<String> candidateEmulatedMachines) {
        String bestMatch = candidateEmulatedMachines
                .stream()
                .max(Comparator.comparingInt(s -> {
                    int index = StringUtils.indexOfDifference(currentEmulatedMachine, s);
                    return index < 0 ? Integer.MAX_VALUE : index;
                }))
                .orElse(currentEmulatedMachine);
        return replaceChipset(bestMatch, chipsetType);
    }

    private static boolean chipsetMatchesEmulatedMachine(ChipsetType chipsetType, String emulatedMachine) {
        return chipsetType == ChipsetType.I440FX || chipsetType == ChipsetType.fromMachineType(emulatedMachine);
    }

    private static String replaceChipset(String emulatedMachine, ChipsetType chipsetType) {
        if (chipsetType != ChipsetType.Q35) {
            return emulatedMachine;
        }

        if (emulatedMachine.contains(I440FX_CHIPSET_NAME)) {
            return emulatedMachine.replace(I440FX_CHIPSET_NAME, Q35_CHIPSET_NAME);
        }

        return emulatedMachine.replace("pc-", "pc-" + Q35_CHIPSET_NAME + '-');
    }

}
