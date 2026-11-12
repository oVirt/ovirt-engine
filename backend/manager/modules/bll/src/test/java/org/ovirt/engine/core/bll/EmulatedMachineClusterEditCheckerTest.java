package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;

/**
 * Tests for {@link EmulatedMachineClusterEditChecker}: warns about hosts that do not
 * support the cluster default(s) introduced by a cluster edit. The cluster default may
 * be the legacy dual format ("i440fx;q35") in clusters below compatibility level 4.8 —
 * the checker must compare against the actual machine types, not the combined string.
 */
public class EmulatedMachineClusterEditCheckerTest {
    private static final String PC_I440FX = "pc-i440fx-rhel7.6.0";
    private static final String PC_Q35 = "pc-q35-rhel9.6.0";

    private static Cluster clusterWithDefault(String emulatedMachine) {
        Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setEmulatedMachine(emulatedMachine);
        return cluster;
    }

    private static VDS hostWithMachines(String machines) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setSupportedEmulatedMachines(machines);
        return vds;
    }

    private EmulatedMachineClusterEditChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new EmulatedMachineClusterEditChecker();
    }

    @Test
    public void dualDefaultRequiresHostToSupportAtLeastOnePart() {
        assertTrue(checker.isApplicable(
                clusterWithDefault(PC_I440FX),
                clusterWithDefault(PC_I440FX + ";" + PC_Q35)));

        // Same semantics as host compliance (VirtMonitoringStrategy): a host supporting
        // one part of the dual default complies — per-VM scheduling handles the rest.
        assertTrue(checker.check(hostWithMachines(PC_I440FX + ",pc-i440fx-rhel9.4.0")));
        assertTrue(checker.check(hostWithMachines(PC_Q35 + ",pc-q35-rhel8.6.0")));

        // A host supporting neither part fails the check.
        assertFalse(checker.check(hostWithMachines("pc-q35-rhel8.0.0")));
    }

    @Test
    public void dualDefaultMustNotBeTreatedAsOneLiteralMachine() {
        assertTrue(checker.isApplicable(
                clusterWithDefault(PC_Q35),
                clusterWithDefault(PC_I440FX + ";" + PC_Q35)));

        // The combined dual string is in no host's machine list — before the fix this
        // host would have been warned about although it supports both actual types.
        assertTrue(checker.check(hostWithMachines(PC_I440FX + "," + PC_Q35)));
    }

    @Test
    public void unchangedDefaultIsNotApplicable() {
        assertFalse(checker.isApplicable(
                clusterWithDefault(PC_I440FX + ";" + PC_Q35),
                clusterWithDefault(PC_I440FX + ";" + PC_Q35)));
    }

    @Test
    public void detailMessageListsMissingMachines() {
        assertTrue(checker.isApplicable(
                clusterWithDefault(PC_Q35),
                clusterWithDefault(PC_I440FX + ";" + PC_Q35)));

        // A host supporting neither part of the new default is warned about exactly the
        // parts it lacks.
        VDS host = hostWithMachines("pc-q35-rhel8.0.0");
        assertFalse(checker.check(host));
        assertEquals(PC_I440FX + "," + PC_Q35, checker.getDetailMessage(host));
    }
}
