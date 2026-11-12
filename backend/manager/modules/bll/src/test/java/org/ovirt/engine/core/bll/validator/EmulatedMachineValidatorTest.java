package org.ovirt.engine.core.bll.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;

/**
 * Tests for {@link EmulatedMachineValidator}.
 */
public class EmulatedMachineValidatorTest {
    private static final String PC_I440FX = "pc-i440fx-rhel7.6.0";
    private static final String PC_Q35 = "pc-q35-rhel9.6.0";
    private static final String S390 = "s390-ccw-virtio-rhel9.4.0";

    private static VmStatic vm(BiosType biosType, String customEmulatedMachine) {
        VmStatic vmStatic = new VmStatic();
        vmStatic.setName("test-vm");
        vmStatic.setBiosType(biosType);
        vmStatic.setCustomEmulatedMachine(customEmulatedMachine);
        return vmStatic;
    }

    private static Cluster cluster(Version version) {
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(version);
        return cluster;
    }

    private static EmulatedMachineValidator validator(BiosType biosType, String customEmulatedMachine, Version version) {
        return new EmulatedMachineValidator(vm(biosType, customEmulatedMachine), cluster(version));
    }

    // --- validateI440fxPinRequired (Add/Update VM: the strict >= 4.8 rule) ---

    @Test
    public void i440fxVmWithoutPinIsRejected() {
        ValidationResult result = validator(BiosType.I440FX_SEA_BIOS, null, Version.v4_8)
                .validateI440fxPinRequired();
        assertFalse(result.isValid());
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_I440FX_VM_REQUIRES_CUSTOM_EMULATED_MACHINE,
                result.getMessages().get(0));
    }

    @Test
    public void i440fxVmWithMatchingPinIsValid() {
        assertTrue(validator(BiosType.I440FX_SEA_BIOS, PC_I440FX, Version.v4_8)
                .validateI440fxPinRequired().isValid());
    }

    @Test
    public void q35VmInheritingClusterDefaultIsValid() {
        assertTrue(validator(BiosType.Q35_SEA_BIOS, null, Version.v4_8)
                .validateI440fxPinRequired().isValid());
    }

    @Test
    public void q35VmWithMatchingPinIsValid() {
        assertTrue(validator(BiosType.Q35_SEA_BIOS, PC_Q35, Version.v4_8)
                .validateI440fxPinRequired().isValid());
    }

    @Test
    public void vmWithoutBiosTypeIsValid() {
        assertTrue(validator(null, PC_I440FX, Version.v4_8)
                .validateI440fxPinRequired().isValid());
    }

    @Test
    public void pinOfWrongChipsetIsRejected() {
        ValidationResult result = validator(BiosType.I440FX_SEA_BIOS, PC_Q35, Version.v4_8)
                .validateChipsetConsistency();
        assertFalse(result.isValid());
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_EMULATED_MACHINE_CHIPSET_MISMATCH,
                result.getMessages().get(0));
    }

    @Test
    public void nonX86PinIsAcceptedForAnyChipset() {
        // s390/pseries machine types return null from ChipsetType.fromMachineType
        assertTrue(validator(BiosType.I440FX_SEA_BIOS, S390, Version.v4_8)
                .validateI440fxPinRequired().isValid());
    }

    // --- validateChipsetConsistency (imports: consistency only, missing pin allowed) ---

    @Test
    public void consistencyCheckAllowsI440fxVmWithoutPin() {
        assertTrue(validator(BiosType.I440FX_SEA_BIOS, null, Version.v4_8)
                .validateChipsetConsistency().isValid());
    }

    @Test
    public void consistencyCheckRejectsWrongChipsetPin() {
        ValidationResult result = validator(BiosType.I440FX_SEA_BIOS, PC_Q35, Version.v4_8)
                .validateChipsetConsistency();
        assertFalse(result.isValid());
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_EMULATED_MACHINE_CHIPSET_MISMATCH,
                result.getMessages().get(0));
    }

    @Test
    public void consistencyCheckAcceptsMatchingPin() {
        assertTrue(validator(BiosType.Q35_SEA_BIOS, PC_Q35, Version.v4_8)
                .validateChipsetConsistency().isValid());
    }

    // --- below cluster level 4.8: the pin is not required, only the consistency check applies ---

    @Test
    public void below48I440fxVmWithoutPinIsValid() {
        // Below 4.8 an unpinned i440fx VM inherits the i440fx part of the dual cluster
        // default — the strict pin requirement must not fire.
        assertTrue(validator(BiosType.I440FX_SEA_BIOS, null, Version.v4_7)
                .validateI440fxPinRequired().isValid());
    }

    @Test
    public void below48ChipsetMismatchIsStillRejected() {
        // The consistency check is unconditional: a pin that contradicts the chipset is
        // always wrong, regardless of the level.
        ValidationResult result = validator(BiosType.I440FX_SEA_BIOS, PC_Q35, Version.v4_7)
                .validateChipsetConsistency();
        assertFalse(result.isValid());
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_EMULATED_MACHINE_CHIPSET_MISMATCH,
                result.getMessages().get(0));
    }

    @Test
    public void below48I440fxVmWithMatchingPinIsValid() {
        assertTrue(validator(BiosType.I440FX_SEA_BIOS, PC_I440FX, Version.v4_7)
                .validateI440fxPinRequired().isValid());
    }
}
