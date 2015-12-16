package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmManagementCommandBase<T extends VmManagementParametersBase> extends VmCommand<T> {

    /**
     * This is the maximum value we can pass to cpuShares according to
     * the virsh man page.
     */
    public static final int MAXIMUM_CPU_SHARES = 262144;

    private InstanceType instanceType;
    private Version effectiveCompatibilityVersion;

    public VmManagementCommandBase(T parameters) {
        super(parameters, null);
    }

    protected VmManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmManagementCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        if (parameters.getVmStaticData() != null) {
            setVmId(parameters.getVmStaticData().getId());
            setVdsGroupId(parameters.getVmStaticData().getVdsGroupId());
        }
    }

    @Override
    protected void init() {
        super.init();
        initEffectiveCompatibilityVersion();
    }

    protected void initEffectiveCompatibilityVersion() {
        setEffectiveCompatibilityVersion(
                CompatibilityVersionUtils.getEffective(getParameters().getVmStaticData(), this::getVdsGroup));
    }

    protected Guid getInstanceTypeId() {
        if (getParameters().getVmStaticData() != null) {
            return getParameters().getVmStaticData().getInstanceTypeId();
        }
        return null;
    }

    protected InstanceType getInstanceType() {
        if (instanceType == null && getInstanceTypeId() != null) {
            instanceType = getVmTemplateDao().getInstanceType(getInstanceTypeId());
        }
        return instanceType;
    }

    protected Version getEffectiveCompatibilityVersion() {
        return effectiveCompatibilityVersion;
    }

    protected void setEffectiveCompatibilityVersion(Version effectiveCompatibilityVersion) {
        this.effectiveCompatibilityVersion = effectiveCompatibilityVersion;
    }

    private final static Pattern cpuPinningPattern =
            Pattern.compile("\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*" +
                    "(_\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*)*");

    /**
     * Checks that a given CPU pinning string is valid Adds an appropriate message to Validate messages if validation
     * fails
     *
     * @param cpuPinning String to validate
     * @param vmStatic   vm data, containing vcpu information
     * @return if the given cpuPinning is valid
     */
    public boolean isCpuPinningValid(final String cpuPinning, VmStatic vmStatic) {

        if (StringUtils.isEmpty(cpuPinning)) {
            return true;
        }

        if (!cpuPinningPattern.matcher(cpuPinning).matches()) {
            // ERROR bad syntax
            addValidationMessage(EngineMessage.VM_PINNING_FORMAT_INVALID);
            return false;
        }

        HashSet<Integer> vcpus = new HashSet<>();
        String[] rules = cpuPinning.split("_");

        int maxvCPU = vmStatic.getNumOfCpus();

        // can not check if no dedicated vds was configured
        if (vmStatic.getDedicatedVmForVdsList().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_WITH_UNDEFINED_HOST);
        }

        // check if vcpu rules are valid
        for (String rule : rules) {
            // [0] vcpu, [1] pcpu
            String[] splitRule = rule.split("#");
            int currVcpu = Integer.parseInt(splitRule[0]);
            if (currVcpu >= maxvCPU) {
                // ERROR maps to a non existent vcpu
                return failValidation(EngineMessage.VM_PINNING_VCPU_DOES_NOT_EXIST);
            }
            if (!vcpus.add(currVcpu)) {
                // ERROR contains more than one definition for the same vcpu
                return failValidation(EngineMessage.VM_PINNING_DUPLICATE_DEFINITION);
            }

            Collection<Integer> currPcpus = parsePCpuPinningNumbers(splitRule[1]);
            if (currPcpus == null) {
                return failValidation(EngineMessage.VM_PINNING_FORMAT_INVALID);
            }

            if (currPcpus.size() == 0) {
                // definition of pcpus is no cpu, e.g 0#1,^1
                return failValidation(EngineMessage.VM_PINNING_PINNED_TO_NO_CPU);
            }
        }

        /**
         * TODO: Validate host.
         * Commit aa53774e785398394f28782089b9fa5d8497efb1 says that online cpu reporting does
         * not properly work for ppc64 and that host validation was therefore removed. Although
         * we could work around this by just letting the validation pass when no information is
         * provided there is one real blocker.
         *
         * The engine is currently only informed about online CPUs via getVdsCapabilities which
         * we do not regularly trigger. This makes the online cpu data very unreliable. We can
         * add host validation as soon as we get updates about the availability of cpu cores.
         */
        return true;
    }

    protected VDS getVds(Guid id) {
        return getVdsDao().get(id);
    }

    private Collection<Integer> parsePCpuPinningNumbers(final String text) {
        try {
            HashSet<Integer> include = new HashSet<>();
            HashSet<Integer> exclude = new HashSet<>();
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
            List<Integer> returnList = new LinkedList<>();
            for (int i = start; i <= end; i++) {
                returnList.add(i);
            }
            return returnList;
        } else {
            return null;
        }
    }

    protected boolean validateCustomProperties(VmStatic vmStaticFromParams, List<String> reasons) {
        return VmPropertiesUtils.getInstance().validateVmProperties(
                getEffectiveCompatibilityVersion(),
                vmStaticFromParams.getCustomProperties(),
                reasons);
    }

    static boolean validatePinningAndMigration(List<String> reasons, VmStatic vmStaticData, String cpuPinning) {
        final boolean cpuPinMigrationEnabled = Boolean.TRUE.equals(Config.<Boolean> getValue(ConfigValues.CpuPinMigrationEnabled));
        if (!cpuPinMigrationEnabled
                && (vmStaticData.getMigrationSupport() == MigrationSupport.MIGRATABLE
                || vmStaticData.getMigrationSupport() == MigrationSupport.IMPLICITLY_NON_MIGRATABLE)
                && StringUtils.isNotEmpty(cpuPinning)) {
            reasons.add(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_AND_MIGRATABLE.toString());
            return false;
        }

        if (vmStaticData.isAutoStartup()
                // VM has to be either migratable
                && (vmStaticData.getMigrationSupport() != MigrationSupport.MIGRATABLE
                // or have multiple hosts (no host means any host) in the pinning list
                && vmStaticData.getDedicatedVmForVdsList().size() == 1)) {
            reasons.add(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_PINNED_TO_HOST.toString());
            return false;
        }
        return true;
    }

    protected boolean isVmWithSameNameExists(String name, Guid storagePoolId) {
        return VmHandler.isVmWithSameNameExistStatic(name, storagePoolId);
    }

    protected boolean isCpuSharesValid(VM vmData) {
        return (vmData.getCpuShares() >= 0 && vmData.getCpuShares() <= MAXIMUM_CPU_SHARES);
    }

    protected boolean setAndValidateCpuProfile() {
        return validate(CpuProfileHelper.setAndValidateCpuProfile(getParameters().getVm().getStaticData(),
                getEffectiveCompatibilityVersion()));
    }

    protected void updateParametersVmFromInstanceType() {
        InstanceType instanceType = getInstanceType();
        VmStatic vmStatic = getParameters().getVmStaticData();
        if (instanceType != null) {
            vmStatic.setMemSizeMb(instanceType.getMemSizeMb());
            vmStatic.setNumOfSockets(instanceType.getNumOfSockets());
            vmStatic.setCpuPerSocket(instanceType.getCpuPerSocket());
            vmStatic.setThreadsPerCpu(instanceType.getThreadsPerCpu());
            vmStatic.setAutoStartup(instanceType.isAutoStartup());

            if (FeatureSupported.isMigrationSupported(getVdsGroup().getArchitecture(), getEffectiveCompatibilityVersion())) {
                vmStatic.setMigrationSupport(instanceType.getMigrationSupport());
            }

            if (FeatureSupported.isIoThreadsSupported(getEffectiveCompatibilityVersion())) {
                vmStatic.setNumOfIoThreads(instanceType.getNumOfIoThreads());
            }

            vmStatic.setMigrationDowntime(instanceType.getMigrationDowntime());
            vmStatic.setPriority(instanceType.getPriority());
            vmStatic.setTunnelMigration(instanceType.getTunnelMigration());

            List<VmDevice> vmDevices = VmDeviceUtils.getMemoryBalloons(instanceType.getId());
            vmStatic.setMinAllocatedMem(instanceType.getMinAllocatedMem());
            if (vmDevices.isEmpty()) {
                getParameters().setBalloonEnabled(false);
            } else if (osRepository.isBalloonEnabled(getParameters().getVmStaticData().getOsId(), getEffectiveCompatibilityVersion())) {
                getParameters().setBalloonEnabled(true);
            }
        }
    }

}
