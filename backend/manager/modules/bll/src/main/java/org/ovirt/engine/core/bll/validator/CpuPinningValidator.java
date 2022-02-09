package org.ovirt.engine.core.bll.validator;

import static org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper.parseCpuPinning;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper.PinnedCpu;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class CpuPinningValidator {

    private static final Pattern cpuPinningPattern =
            Pattern.compile("\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*" +
                    "(_\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*)*");

    /**
     * Check if the cpu pinning rules are syntactically correct
     *
     * @param cpuPinning rule description string
     * @return true if valid
     */
    public static boolean isValidCpuPinningSyntax(final String cpuPinning) {
        return cpuPinningPattern.matcher(cpuPinning).matches();
    }

    /**
     * Checks that a given CPU pinning string is valid. Adds an appropriate message to CanDoAction messages if
     * validation fails
     *
     * @param cpuPinning String to validate
     * @param vmBase   vm data, containing vcpu information
     * @return if the given cpuPinning is valid
     */
    public static ValidationResult isCpuPinningValid(final String cpuPinning, VmBase vmBase) {
        if (StringUtils.isEmpty(cpuPinning)) {
            return ValidationResult.VALID;
        }

        if (vmBase.getCpuPinningPolicy() != CpuPinningPolicy.MANUAL) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_MANUAL_PINNING_POLICY_NOT_SELECTED);
        }

        if (!isValidCpuPinningSyntax(cpuPinning)) {
            return new ValidationResult(EngineMessage.VM_PINNING_FORMAT_INVALID);
        }

        // check if no dedicated vds was configured
        if (vmBase.getDedicatedVmForVdsList().isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_WITH_UNDEFINED_HOST);
        }

        Set<Integer> assignedVCpus = new HashSet<>();
        int maxvCPU = vmBase.getNumOfCpus();

        // check if vcpu rules are valid
        for (PinnedCpu pinnedCpu : parseCpuPinning(cpuPinning)) {
            int vCpu = pinnedCpu.getvCpu();
            if (vCpu >= maxvCPU) {
                return new ValidationResult(EngineMessage.VM_PINNING_VCPU_DOES_NOT_EXIST);
            }
            if (!assignedVCpus.add(vCpu)) {
                return new ValidationResult(EngineMessage.VM_PINNING_DUPLICATE_DEFINITION);
            }

            if (pinnedCpu.getpCpus().isEmpty()) {
                // definition of pcpus is no cpu, e.g 0#1,^1
                return new ValidationResult(EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
            }
        }

        return ValidationResult.VALID;
    }

}
