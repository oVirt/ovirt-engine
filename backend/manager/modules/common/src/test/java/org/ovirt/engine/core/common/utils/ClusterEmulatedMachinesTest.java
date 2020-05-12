package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ChipsetType;

public class ClusterEmulatedMachinesTest {

    public static final String PC_I440FX_RHEL = "pc-i440fx-rhel7.6.0";
    public static final String PC_Q35_RHEL = "pc-q35-rhel7.6.0";
    public static final String PC = "pc-2.2";
    public static final String PC_I440FX = "pc-i440fx-2.2";
    public static final String PC_Q35 = "pc-q35-2.2";
    public static final String PSERIES = "pseries-rhel8.1.0";

    @Test
    public void testReplaceChipset() {
        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.replaceChipset(PC_I440FX_RHEL, ChipsetType.Q35));
        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.replaceChipset(PC_Q35_RHEL, ChipsetType.Q35));
        assertEquals(PC_Q35, ClusterEmulatedMachines.replaceChipset(PC, ChipsetType.Q35));
        assertEquals(PSERIES, ClusterEmulatedMachines.replaceChipset(PSERIES, ChipsetType.Q35));
        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.replaceChipset(PC_Q35_RHEL, ChipsetType.I440FX));
        assertEquals(PC_I440FX, ClusterEmulatedMachines.replaceChipset(PC_Q35, ChipsetType.I440FX));
        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.replaceChipset(PC_I440FX_RHEL, ChipsetType.I440FX));
        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.replaceChipset(PC_I440FX_RHEL, null));
    }

}
