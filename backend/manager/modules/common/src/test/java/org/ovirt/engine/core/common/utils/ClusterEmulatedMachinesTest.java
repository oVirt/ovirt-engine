package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.compat.Version;

public class ClusterEmulatedMachinesTest {

    public static final String PC_I440FX_RHEL = "pc-i440fx-rhel7.6.0";
    public static final String PC_Q35_RHEL = "pc-q35-rhel9.6.0";
    public static final String PSERIES = "pseries-rhel8.1.0";

    // --- chipsetMatches ---

    @Test
    public void testChipsetMatchesQ35() {
        assertTrue(ClusterEmulatedMachines.chipsetMatches(ChipsetType.Q35).test(PC_Q35_RHEL));
    }

    @Test
    public void testChipsetMatchesI440fx() {
        assertTrue(ClusterEmulatedMachines.chipsetMatches(ChipsetType.I440FX).test(PC_I440FX_RHEL));
    }

    @Test
    public void testChipsetMatchesNonX86() {
        // Non-x86 machine types return null from ChipsetType.fromMachineType(),
        // which is treated as I440FX-compatible (legacy behavior)
        assertTrue(ClusterEmulatedMachines.chipsetMatches(ChipsetType.I440FX).test(PSERIES));
    }

    // --- getSupportedByChipset ---

    @Test
    public void testGetSupportedByChipsetQ35() {
        Set<String> supported = new HashSet<>(Arrays.asList(PC_Q35_RHEL, PC_I440FX_RHEL));
        List<String> available = Arrays.asList(PC_Q35_RHEL, PC_I440FX_RHEL, PSERIES);
        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.getSupportedByChipset(ChipsetType.Q35, supported, available));
    }

    @Test
    public void testGetSupportedByChipsetNotSupported() {
        Set<String> supported = new HashSet<>(Collections.singletonList(PC_I440FX_RHEL));
        List<String> available = Arrays.asList(PC_Q35_RHEL, PC_I440FX_RHEL);
        // Host doesn't support Q35
        assertEquals(null, ClusterEmulatedMachines.getSupportedByChipset(ChipsetType.Q35, supported, available));
    }

    // --- pickImportI440fxMachine (auto-pin of imported i440fx VMs) ---

    @Test
    public void testPickNewVmI440fxMachineNewestCommonType() {
        // newest: a new VM has no history to preserve and gets the longest support runway
        assertEquals("pc-i440fx-rhel10.0.0",
                ClusterEmulatedMachines.pickNewVmI440fxMachine(
                        Arrays.asList("pc-i440fx-rhel10.0.0", PC_I440FX_RHEL)));
        // the floating 'pc' alias is never a stable pin
        assertEquals(PC_I440FX_RHEL,
                ClusterEmulatedMachines.pickNewVmI440fxMachine(
                        Arrays.asList("pc", PC_I440FX_RHEL)));
        assertNull(ClusterEmulatedMachines.pickNewVmI440fxMachine(Collections.singletonList("pc")));
        assertNull(ClusterEmulatedMachines.pickNewVmI440fxMachine(Collections.emptyList()));
        assertNull(ClusterEmulatedMachines.pickNewVmI440fxMachine(null));
    }

    @Test
    public void testPickImportI440fxMachineOldestCommonType() {
        // numeric ordering: rhel7.6.0 is older than rhel10.0.0
        assertEquals(PC_I440FX_RHEL,
                ClusterEmulatedMachines.pickImportI440fxMachine(
                        Arrays.asList("pc-i440fx-rhel10.0.0", PC_I440FX_RHEL)));
    }

    @Test
    public void testPickImportI440fxMachineExcludesFloatingPcAlias() {
        assertEquals("pc-i440fx-rhel10.0.0",
                ClusterEmulatedMachines.pickImportI440fxMachine(
                        Arrays.asList("pc", "pc-i440fx-rhel10.0.0")));
        // only the alias in common: no stable pin can be derived
        assertNull(ClusterEmulatedMachines.pickImportI440fxMachine(Collections.singletonList("pc")));
    }

    @Test
    public void testPickImportI440fxMachineUpstreamStyleVersion() {
        // upstream-style version (2.12) sorts before rhel7.6.0 numerically
        assertEquals("pc-i440fx-2.12",
                ClusterEmulatedMachines.pickImportI440fxMachine(
                        Arrays.asList(PC_I440FX_RHEL, "pc-i440fx-2.12")));
    }

    @Test
    public void testPickImportI440fxMachineNoDerivableType() {
        assertNull(ClusterEmulatedMachines.pickImportI440fxMachine(Collections.emptyList()));
        assertNull(ClusterEmulatedMachines.pickImportI440fxMachine(null));
    }

    // --- getEffective ---

    @Test
    public void testGetEffectiveCustomEmulatedMachine() {
        VM vm = new VM();
        vm.setCustomEmulatedMachine(PC_I440FX_RHEL);
        Cluster cluster = new Cluster();
        cluster.setEmulatedMachine(PC_Q35_RHEL);

        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveQ35VmQ35OnlyCluster() {
        VM vm = new VM();
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setEmulatedMachine(PC_Q35_RHEL);

        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveI440fxVmNoCustomEmulatedMachine() {
        VM vm = new VM();
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setEmulatedMachine(PC_Q35_RHEL);

        assertThrows(IllegalStateException.class,
                () -> ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveI440fxVmWithCustomEmulatedMachine() {
        VM vm = new VM();
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        vm.setCustomEmulatedMachine(PC_I440FX_RHEL);
        Cluster cluster = new Cluster();
        cluster.setEmulatedMachine(PC_Q35_RHEL);

        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveQ35VmLegacyDualFormatClusterDefault() {
        // Pre-migration dual-format default: a Q35 VM must get the Q35 part, not the whole
        // (or the i440fx part of the) legacy value.
        VM vm = new VM();
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setEmulatedMachine(PC_I440FX_RHEL + ";" + PC_Q35_RHEL);

        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveQ35VmI440fxOnlyClusterDefaultThrows() {
        // Single-i440fx cluster default (old cluster level or inconsistent DB): a Q35 VM must
        // not silently run with an i440fx machine type.
        VM vm = new VM();
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setEmulatedMachine(PC_I440FX_RHEL);

        assertThrows(IllegalStateException.class,
                () -> ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveLegacyVmNullBiosTypeKeepsI440fxDefault() {
        // Old-cluster VMs predate chipset awareness (biosType == null) and must keep
        // receiving the i440fx cluster default as-is (at a sub-4.8 cluster level).
        VM vm = new VM();
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine(PC_I440FX_RHEL);

        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    // --- findBestMatchForEmulatedMachine ---

    @Test
    public void testFindBestMatchForEmulatedMachine() {
        String original = "pc-i440fx-rhel7.3.0";
        String bestMatch = "pc-i440fx-rhel7.2.0";
        List<String> candidates = Arrays.asList("pc-i440fx-2.1", bestMatch, "pseries-rhel7.2.0");
        assertEquals(bestMatch,
                ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.I440FX, original, candidates));
    }

    @Test
    public void testFindBestMatchForEmulatedMachineKeepsCurrent() {
        String original = "pc-i440fx-rhel7.3.0";
        List<String> candidates = Arrays.asList("pc-i440fx-2.1", original, "pseries-rhel7.2.0");
        assertEquals(original,
                ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.I440FX, original, candidates));
    }

    @Test
    public void testFindBestMatchForEmulatedMachineQ35() {
        String bestMatch = "pc-i440fx-rhel7.5.0";
        String desired = "pc-q35-rhel7.5.0";
        List<String> candidates = Arrays.asList("pc-i440fx-2.1", bestMatch, desired, "pseries-rhel7.2.0");
        assertEquals(desired,
                ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.Q35, bestMatch, candidates));
    }

    @Test
    public void testFindBestMatchForEmulatedMachineNoMatch() {
        String original = "pc-q35-rhel9.6.0";
        List<String> candidates = Arrays.asList("pc-i440fx-rhel7.6.0", "pseries-rhel8.1.0");
        assertEquals(original,
                ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.Q35, original, candidates));
    }

    // --- commonPrefixLength (tested via findBestMatchForEmulatedMachine) ---

    @Test
    public void testIndexOfDifferenceIdentical() {
        // Identical strings should rank highest (MAX_VALUE) — tested indirectly:
        // when the current machine is in the candidate list, it should be returned.
        String s = "pc-q35-rhel9.6.0";
        List<String> candidates = Collections.singletonList(s);
        assertEquals(s, ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.Q35, s, candidates));
    }

    @Test
    public void testIndexOfDifferenceEmptyCurrent() {
        // Empty current should match the first candidate (commonPrefixLength returns 0 for empty)
        List<String> candidates = Arrays.asList("pc-q35-rhel9.6.0", "pc-i440fx-rhel7.6.0");
        // With empty current, all candidates have commonPrefixLength=0, so first matching is returned
        String result = ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.Q35, "", candidates);
        assertEquals("pc-q35-rhel9.6.0", result);
    }

    @Test
    public void testIndexOfDifferenceDifferentLengths() {
        // Longer and shorter candidates: the one with longer common prefix should win
        String current = "pc-q35-rhel9.6.0";
        List<String> candidates = Arrays.asList("pc-q35-rhel10.0.0", "pc-q35-4.1");
        // "pc-q35-rhel10.0.0" differs at index 11 (index of '1' in '10')
        // "pc-q35-4.1" differs at index 7 (index of '4' in '4.1')
        // So rhel10.0.0 should win (longer common prefix)
        assertEquals("pc-q35-rhel10.0.0",
                ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.Q35, current, candidates));
    }

    @Test
    public void testIndexOfDifferenceNullCurrent() {
        // Null current: commonPrefixLength returns 0, so all candidates are equal rank
        // First matching candidate should be returned
        List<String> candidates = Arrays.asList("pc-q35-rhel9.6.0", "pc-q35-rhel10.0.0");
        String result = ClusterEmulatedMachines.findBestMatchForEmulatedMachine(ChipsetType.Q35, null, candidates);
        assertEquals("pc-q35-rhel9.6.0", result);
    }

    // --- propagateI440fxPin (pending configurations that predate the pinning) ---

    private static VmStatic unpinnedI440fxConfig(String name) {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setName(name);
        vmStatic.setBiosType(BiosType.I440FX_SEA_BIOS);
        return vmStatic;
    }

    @Test
    public void testPropagateI440fxPinToUnpinnedPendingConfig() {
        // A NEXT_RUN snapshot created before the migration/raise carries no pin; the
        // entity's current pin (its effective hardware) is propagated onto it.
        VmStatic pending = unpinnedI440fxConfig("pending");

        ClusterEmulatedMachines.propagateI440fxPin(PC_I440FX_RHEL, pending);

        assertEquals(PC_I440FX_RHEL, pending.getCustomEmulatedMachine());
    }

    @Test
    public void testPropagateI440fxPinKeepsPendingExplicitPin() {
        VmStatic pending = unpinnedI440fxConfig("pending");
        pending.setCustomEmulatedMachine("pc-i440fx-rhel9.4.0");

        ClusterEmulatedMachines.propagateI440fxPin(PC_I440FX_RHEL, pending);

        assertEquals("pc-i440fx-rhel9.4.0", pending.getCustomEmulatedMachine(),
                "an explicit pin in the pending configuration must be kept");
    }

    @Test
    public void testPropagateI440fxPinIgnoresQ35AndNullBiosPendingConfigs() {
        VmStatic q35Pending = unpinnedI440fxConfig("q35-pending");
        q35Pending.setBiosType(BiosType.Q35_SEA_BIOS);

        ClusterEmulatedMachines.propagateI440fxPin(PC_I440FX_RHEL, q35Pending);
        assertNull(q35Pending.getCustomEmulatedMachine(),
                "a Q35 pending configuration must not receive an i440fx pin");

        VmStatic nullBiosPending = new VmStatic();
        nullBiosPending.setName("legacy-pending");
        ClusterEmulatedMachines.propagateI440fxPin(PC_I440FX_RHEL, nullBiosPending);
        assertNull(nullBiosPending.getCustomEmulatedMachine(),
                "a pending configuration without chipset info must be left alone");
    }

    @Test
    public void testPropagateI440fxPinRequiresNonEmptyPin() {
        VmStatic pending = unpinnedI440fxConfig("pending");

        ClusterEmulatedMachines.propagateI440fxPin(null, pending);
        ClusterEmulatedMachines.propagateI440fxPin("", pending);

        assertNull(pending.getCustomEmulatedMachine());
    }

    @AfterEach
    public void restoreConfig() {
        Config.setConfigUtils(null);
    }

    // --- isI440fxDeprecated ---

    @Test
    public void testIsI440fxDeprecatedBoundaries() {
        assertFalse(ClusterEmulatedMachines.isI440fxDeprecated(null));
        assertFalse(ClusterEmulatedMachines.isI440fxDeprecated(new Version(4, 7)));
        assertTrue(ClusterEmulatedMachines.isI440fxDeprecated(new Version(4, 8)));
        assertTrue(ClusterEmulatedMachines.isI440fxDeprecated(new Version(4, 9)));
        assertTrue(ClusterEmulatedMachines.isI440fxDeprecated(Version.getLast()));
    }

    // --- defaultForChipset / build ---

    @Test
    public void testDefaultForChipsetDualFormat() {
        String dual = PC_I440FX_RHEL + ";" + PC_Q35_RHEL;
        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.defaultForChipset(dual, ChipsetType.I440FX));
        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.defaultForChipset(dual, ChipsetType.Q35));
    }

    @Test
    public void testDefaultForChipsetSingleValueAsIs() {
        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.defaultForChipset(PC_Q35_RHEL, ChipsetType.I440FX));
        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.defaultForChipset(PC_I440FX_RHEL, ChipsetType.Q35));
    }

    @Test
    public void testDefaultForChipsetEmptyPartAndNull() {
        assertNull(ClusterEmulatedMachines.defaultForChipset(null, ChipsetType.I440FX));
        assertNull(ClusterEmulatedMachines.defaultForChipset(";" + PC_Q35_RHEL, ChipsetType.I440FX));
        assertNull(ClusterEmulatedMachines.defaultForChipset(PC_I440FX_RHEL + ";", ChipsetType.Q35));
    }

    @Test
    public void testBuild() {
        String dual = PC_I440FX_RHEL + ";" + PC_Q35_RHEL;
        assertEquals(dual, ClusterEmulatedMachines.build(PC_I440FX_RHEL, PC_Q35_RHEL));
        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.build(PC_I440FX_RHEL, null));
        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.build(null, PC_Q35_RHEL));
        assertNull(ClusterEmulatedMachines.build(null, null));
    }

    // --- getEffective below 4.8 (dual format is a supported state) ---

    @Test
    public void testGetEffectiveBelow48UnpinnedI440fxVmInheritsDualPart() {
        // The key behavior of the per-cluster design: below 4.8 an unpinned i440fx VM
        // inherits the i440fx part of the dual cluster default instead of throwing.
        VM vm = new VM();
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine(PC_I440FX_RHEL + ";" + PC_Q35_RHEL);

        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveBelow48UnpinnedQ35VmInheritsDualPart() {
        VM vm = new VM();
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine(PC_I440FX_RHEL + ";" + PC_Q35_RHEL);

        assertEquals(PC_Q35_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveBelow48I440fxVmSingleQ35DefaultThrows() {
        // The resolved default must be chipset-checked: a single-Q35 cluster default must
        // not silently pin/run a Q35 machine onto an i440fx VM (mirrors the raise-time
        // pin guard).
        VM vm = new VM();
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine(PC_Q35_RHEL);

        assertThrows(IllegalStateException.class,
                () -> ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveBelow48I440fxVmWithCcvGetsBestMatchForItsVersion() {
        // An i440fx VM pinned to an older compatibility version must get the best match
        // from that version's machine list, not the current cluster default (M5).
        VM vm = new VM();
        vm.setBiosType(BiosType.I440FX_SEA_BIOS);
        vm.getStaticData().setCustomCompatibilityVersion(Version.v4_5);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine("pc-i440fx-rhel8.6.0;" + PC_Q35_RHEL);

        IConfigUtilsInterface configUtils = mock(IConfigUtilsInterface.class);
        doReturn(Arrays.asList("pc-i440fx-rhel7.3.0", "pc-i440fx-rhel8.4.0"))
                .when(configUtils).getValue(ConfigValues.ClusterEmulatedMachines, "4.5");
        Config.setConfigUtils(configUtils);

        // "pc-i440fx-rhel8.4.0" shares the longest common prefix with the resolved
        // default ("pc-i440fx-rhel8.6.0") — the best match for the VM's version.
        assertEquals("pc-i440fx-rhel8.4.0", ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveBelow48LegacyVmNullBiosTypeResolvesDualI440fxPart() {
        // Legacy VMs (biosType == null) predate chipset awareness and run i440fx hardware:
        // a dual default must never reach libvirt raw — resolve its i440fx part.
        VM vm = new VM();
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_7);
        cluster.setEmulatedMachine(PC_I440FX_RHEL + ";" + PC_Q35_RHEL);

        assertEquals(PC_I440FX_RHEL, ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }

    @Test
    public void testGetEffectiveNullClusterDefaultPassesThrough() {
        // A null cluster default is an inconsistent state in itself (no host complies);
        // getEffective must not throw before the run-time compliance check does.
        VM vm = new VM();
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(Version.v4_8);
        cluster.setEmulatedMachine(null);

        assertNull(ClusterEmulatedMachines.getEffective(vm, () -> cluster));
    }
}
