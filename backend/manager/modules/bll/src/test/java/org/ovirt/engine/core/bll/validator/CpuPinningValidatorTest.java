package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ovirt.engine.core.bll.validator.CpuPinningValidator.isCpuPinningValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class CpuPinningValidatorTest {

    private VmStatic vmStatic;

    @BeforeEach
    public void setUp() {
        vmStatic = new VmStatic();
        vmStatic.setNumOfSockets(6);
        vmStatic.setCpuPerSocket(2);
        vmStatic.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        vmStatic.setDedicatedVmForVdsList(Collections.singletonList(Guid.Empty));
    }

    @Test
    public void isCpuPinningValidWithoutManualPolicySelected() {
        vmStatic.setCpuPinningPolicy(CpuPinningPolicy.NONE);
        assertFailsWith(isCpuPinningValid("0#0", vmStatic),
                EngineMessage.ACTION_TYPE_FAILED_VM_MANUAL_PINNING_POLICY_NOT_SELECTED);
    }

    @Test
    public void isCpuPinningValidWithoutPinnedHost() {
        vmStatic.setDedicatedVmForVdsList(new ArrayList<>());
        assertFailsWith(isCpuPinningValid("0#0", vmStatic),
                EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_WITH_UNDEFINED_HOST);
    }

    @Test
    public void isCpuPinningValidWithMultiplePinnedHosts() {
        vmStatic.setDedicatedVmForVdsList(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        assertTrue(isCpuPinningValid("0#0", vmStatic).isValid());
    }

    @Test
    public void schouldDetectValidPinnings() {
        assertTrue(
                isCpuPinningValid(null, vmStatic).isValid(), "null value must be accepted");

        assertTrue(
                isCpuPinningValid("", vmStatic).isValid(), "empty string must be accepted");

        assertTrue(isCpuPinningValid("0#0", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-4", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-8,^6", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-8,^6,^7", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-8,^6,^7", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-8,^5,^6,^7", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1,2,3", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-4,6-8", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-4,6-8,9-12", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-4,^3,9-12,^10", vmStatic).isValid());

        assertTrue(isCpuPinningValid("0#0_1#1", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1-2_1#1-2", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1,2,3_1#2,3", vmStatic).isValid());
        assertTrue(isCpuPinningValid("0#1,2,3_1#1-4,^3", vmStatic).isValid());
        //validate vcpus over 9
        assertTrue(isCpuPinningValid("10#1,2,3_11#1-4,^3", vmStatic).isValid());
        assertTrue(isCpuPinningValid("10#1,2,3_11#1-20,^3", vmStatic).isValid());
    }

    @Test
    public void shouldDetectInvalidPinnings() {
        assertFailsWith(isCpuPinningValid("intentionally invalid", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith("random wrong text",
                isCpuPinningValid("lorem ipsum", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith("no cpu id specified, should not pass",
                isCpuPinningValid("0", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith("letter instead of vcpu ID",
                isCpuPinningValid("A#1", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith("letter instead of cpu ID",
                isCpuPinningValid("0#B", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith(isCpuPinningValid("0#1_", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith("Trailing _",
                isCpuPinningValid("0#1_1#2_", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith("Too many separators",
                isCpuPinningValid("0#1__1#2", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith("trailing junk",
                isCpuPinningValid("0#1_1#2...", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
        assertFailsWith(isCpuPinningValid("0#1-8^5", vmStatic),
                EngineMessage.VM_PINNING_FORMAT_INVALID);
    }

    @Test
    public void shouldDetectLogicalInvalidPinnings() {
        // negative logical validation
        assertFailsWith(isCpuPinningValid("10#1,2,3_10#1-4,^3", vmStatic),
                EngineMessage.VM_PINNING_DUPLICATE_DEFINITION);
        assertFailsWith(isCpuPinningValid("10#1,2,^1,^2", vmStatic),
                EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
        assertFailsWith(isCpuPinningValid("10#1,2,3_20#1-4,^3", vmStatic),
                EngineMessage.VM_PINNING_VCPU_DOES_NOT_EXIST);
    }

    @Test
    public void shouldNotAcceptInvalidRange() {
        assertFailsWith(isCpuPinningValid("0#8-1,^5,^6,^7", vmStatic),
                EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
        assertFailsWith(isCpuPinningValid("0#1-1", vmStatic),
                EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
    }

    @Test
    public void shouldDetectInvalidExclude() {
        assertFailsWith(isCpuPinningValid("0#^3", vmStatic),
                EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
        assertFailsWith(isCpuPinningValid("0#^3,^2", vmStatic),
                EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
        assertFailsWith(isCpuPinningValid("0#^1,^2,^3", vmStatic),
                EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
    }

    private void assertFailsWith(ValidationResult validationResult, EngineMessage engineMessage) {
        assertThat(validationResult, failsWith(engineMessage));
    }

    private void assertFailsWith(String errorMessage, ValidationResult validationResult, EngineMessage engineMessage) {
        assertThat(errorMessage, validationResult, failsWith(engineMessage));
    }
}
