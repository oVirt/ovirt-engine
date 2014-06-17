package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmManagementCommandBase<T extends VmManagementParametersBase> extends VmCommand<T> {

    /**
     * This is the maximum value we can pass to cpuShares according to
     * the virsh man page.
     */
    public static final int MAXIMUM_CPU_SHARES = 262144;

    public VmManagementCommandBase(T parameters) {
        super(parameters, null);
    }

    protected VmManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmManagementCommandBase(T parameters, CommandContext commandContext) {
        super(parameters);
        if (parameters.getVmStaticData() != null) {
            setVmId(parameters.getVmStaticData().getId());
            setVdsGroupId(parameters.getVmStaticData().getVdsGroupId());
        }
    }

    /**
     * Checks that dedicated host is on the same cluster as the VM
     *
     * @param vm
     *            - the VM to check
     * @return
     */
    protected boolean isDedicatedVdsOnSameCluster(VmStatic vm) {
        boolean result = true;
        if (vm.getDedicatedVmForVds() != null) {
            // get dedicated host id
            Guid guid = vm.getDedicatedVmForVds();
            // get dedicated host cluster and comparing it to VM cluster
            VDS vds = getVdsDAO().get(guid);
            result = vds != null && (Objects.equals(vm.getVdsGroupId(), vds.getVdsGroupId()));
        }
        if (!result) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.ACTION_TYPE_FAILED_DEDICATED_VDS_NOT_IN_SAME_CLUSTER.toString());
        }
        return result;
    }

    private final static Pattern cpuPinningPattern =
            Pattern.compile("\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*" +
                    "(_\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*)*");

    /**
     * Checks that a given CPU pinning string is valid Adds an appropriate message to CanDoAction messages if validation
     * fails
     *
     * @param cpuPinning
     *            String to validate
     * @param maxVcpus
     *            Number of vCPUs in the VM
     * @param maxPcpus
     *            Number of pCPUs in the host
     * @return if the given cpuPinning is valid
     */
    public boolean isCpuPinningValid(final String cpuPinning, VmStatic vmStatic) {


        if (StringUtils.isEmpty(cpuPinning)) {
            return true;
        }

        if (!cpuPinningPattern.matcher(cpuPinning).matches()) {
            // ERROR bad syntax
            addCanDoActionMessage(VdcBllMessages.VM_PINNING_FORMAT_INVALID);
            return false;
        }

        HashSet<Integer> vcpus = new HashSet<Integer>();
        String[] rules = cpuPinning.split("_");

        int maxvCPU = vmStatic.getNumOfCpus();


        for (String rule : rules) {
            // [0] vcpu, [1] pcpu
            String[] splitRule = rule.split("#");
            int currVcpu = Integer.parseInt(splitRule[0]);
            if (currVcpu >= maxvCPU) {
                // ERROR maps to a non existent vcpu
                return failCanDoAction(VdcBllMessages.VM_PINNING_VCPU_DOES_NOT_EXIST);
            }
            if (!vcpus.add(currVcpu)) {
                // ERROR contains more then one definition for the same vcpu
                return failCanDoAction(VdcBllMessages.VM_PINNING_DUPLICATE_DEFINITION);
            }

            Collection<Integer> currPcpus = parsePCpuPinningNumbers(splitRule[1]);
            if (currPcpus == null) {
                return failCanDoAction(VdcBllMessages.VM_PINNING_FORMAT_INVALID);
            }

            if (currPcpus.size() == 0) {
                // definition of pcpus is no cpu, e.g 0#1,^1
                return failCanDoAction(VdcBllMessages.VM_PINNING_PINNED_TO_NO_CPU);
            }

            // can not check if no dedicated vds was configured
            if (vmStatic.getDedicatedVmForVds() != null) {
                VDS dedicatedVds = getVds(vmStatic.getDedicatedVmForVds());
                // check only from cluster version 3.2
                if (dedicatedVds != null &&
                        dedicatedVds.getVdsGroupCompatibilityVersion() != null &&
                        dedicatedVds.getVdsGroupCompatibilityVersion().compareTo(Version.v3_2) >= 0 &&
                        dedicatedVds.getCpuThreads() != null) {
                    if (Collections.max(currPcpus) >= dedicatedVds.getCpuThreads()) {
                        // ERROR maps to a non existent pcpu
                        return failCanDoAction(VdcBllMessages.VM_PINNING_PCPU_DOES_NOT_EXIST);
                    }
                }
            } else {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_WITH_UNDEFINED_HOST);
            }
        }

        return true;
    }

    protected VDS getVds(Guid id) {
        return getVdsDAO().get(id);
    }

    private Collection<Integer> parsePCpuPinningNumbers(final String text) {
        try {
            HashSet<Integer> include = new HashSet<Integer>();
            HashSet<Integer> exclude = new HashSet<Integer>();
            String[] splitText = text.split(",");
            for (String section : splitText) {
                if (section.startsWith("^")) {
                    exclude.add(Integer.parseInt(section.substring(1)));
                } else if (section.contains("-")) {
                    // include range
                    String[] numbers = section.split("-");
                    int start = Integer.parseInt(numbers[0]);
                    int end = Integer.parseInt(numbers[1]);
                    List<Integer> range = createRange(start, end);
                    if (range != null) {
                        include.addAll(range);
                    } else {
                        return null;
                    }
                } else {
                    // include one
                    include.add(Integer.parseInt(section));
                }
            }
            include.removeAll(exclude);
            return include;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Integer> createRange(int start, int end) {
        if (start >= 0 && start < end) {
            List<Integer> returnList = new LinkedList<Integer>();
            for (int i = start; i <= end; i++) {
                returnList.add(i);
            }
            return returnList;
        } else {
            return null;
        }
    }

    static boolean validatePinningAndMigration(List<String> reasons, VmStatic vmStaticData, String cpuPinning) {
        final boolean cpuPinMigrationEnabled = Boolean.TRUE.equals(Config.<Boolean> getValue(ConfigValues.CpuPinMigrationEnabled));
        if (!cpuPinMigrationEnabled
                && (vmStaticData.getMigrationSupport() == MigrationSupport.MIGRATABLE
                || vmStaticData.getMigrationSupport() == MigrationSupport.IMPLICITLY_NON_MIGRATABLE)
                && StringUtils.isNotEmpty(cpuPinning)) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_AND_MIGRATABLE.toString());
            return false;
        }

        if (vmStaticData.isAutoStartup() && vmStaticData.getMigrationSupport() != MigrationSupport.MIGRATABLE) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_PINNED_TO_HOST.toString());
            return false;
        }
        return true;
    }

    protected boolean isVmWithSameNameExists(String name) {
        return VmHandler.isVmWithSameNameExistStatic(name);
    }

    protected boolean isCpuSharesValid(VM vmData) {
        return (vmData.getCpuShares() >= 0 && vmData.getCpuShares() <= MAXIMUM_CPU_SHARES);
    }
}
