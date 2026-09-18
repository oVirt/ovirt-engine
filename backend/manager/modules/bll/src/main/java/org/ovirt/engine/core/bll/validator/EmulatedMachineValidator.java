package org.ovirt.engine.core.bll.validator;

import org.apache.commons.lang3.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ClusterEmulatedMachines;

/**
 * Validates a VM's emulated machine pin against its chipset and, on the strict
 * >= 4.8 paths, against the pin-required rule. The validator is stateful: it is
 * constructed with the VM configuration and target cluster (typically in the
 * command's {@code init()}), and the level-dependent rule is derived once —
 * mirroring {@code ClusterValidator}.
 * <p>
 * Since the cluster default is Q35 at >= 4.8, i440fx VMs must carry an explicit
 * machine type ({@code custom_emulated_machine}); the full check (pin required +
 * consistency) applies to Add/Update VM. Imports apply the consistency check
 * only — an unpinned i440fx import is auto-pinned from the cluster's common
 * i440fx machine types when possible (see
 * {@code ImportVmCommandBase.autoPinImportedI440fxMachine}), and otherwise fails
 * with a clear message when run.
 */
public class EmulatedMachineValidator {

    private final VmStatic vmStatic;
    private final boolean i440fxPinRequired;

    /**
     * @param vmStatic the VM configuration to validate
     * @param cluster the target cluster; its compatibility version decides whether
     *        the strict pin-required rule applies (true for level >= 4.8)
     */
    public EmulatedMachineValidator(VmStatic vmStatic, Cluster cluster) {
        this.vmStatic = vmStatic;
        this.i440fxPinRequired = cluster != null
                && ClusterEmulatedMachines.isI440fxDeprecated(vmStatic, cluster);
    }

    /**
     * Whether the strict >= 4.8 rule applies: an i440fx VM without an explicit machine
     * type is rejected. Below 4.8 the VM inherits the i440fx part of the dual cluster
     * default, so no pin is required.
     */
    public boolean isI440fxPinRequired() {
        return i440fxPinRequired;
    }

    /**
     * The strict >= 4.8 rule (see {@link #isI440fxPinRequired()}). Used by
     * AddVmCommand/UpdateVmCommand (the import paths auto-pin instead, see
     * EmulatedMachinePinningHandler).
     *
     * @return a failed result with an actionable message, or {@link ValidationResult#VALID}
     */
    public ValidationResult validateI440fxPinRequired() {
        BiosType biosType = vmStatic.getBiosType();
        if (!i440fxPinRequired || biosType == null || biosType.getChipsetType() != ChipsetType.I440FX) {
            return ValidationResult.VALID;
        }
        if (StringUtils.isEmpty(vmStatic.getCustomEmulatedMachine())) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_I440FX_VM_REQUIRES_CUSTOM_EMULATED_MACHINE,
                    String.format("$vmName %1$s", vmStatic.getName()));
        }
        return ValidationResult.VALID;
    }

    /**
     * Validates only that the VM's emulated machine pin matches its chipset. Used by the
     * import commands: an i440fx VM without a pin is imported as-is (auto-pinned when
     * possible, and otherwise it fails with a clear message when run), but a pin that
     * contradicts the chipset is always wrong, regardless of the level.
     */
    public ValidationResult validateChipsetConsistency() {
        BiosType biosType = vmStatic.getBiosType();
        String customEmulatedMachine = vmStatic.getCustomEmulatedMachine();
        if (biosType == null || StringUtils.isEmpty(customEmulatedMachine)) {
            return ValidationResult.VALID;
        }
        ChipsetType machineChipset = ChipsetType.fromMachineType(customEmulatedMachine);
        if (machineChipset != null && machineChipset != biosType.getChipsetType()) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_EMULATED_MACHINE_CHIPSET_MISMATCH,
                    String.format("$vmName %1$s", vmStatic.getName()));
        }
        return ValidationResult.VALID;
    }
}
