package org.ovirt.engine.core.bll;

import java.util.List;

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
import org.ovirt.engine.core.compat.Guid;

public class VmManagementCommandBase<T extends VmManagementParametersBase> extends VmCommand<T> {

    /**
     * This is the maximum value we can pass to cpuShares according to
     * the virsh man page.
     */
    public static final int MAXIMUM_CPU_SHARES = 262144;

    private InstanceType instanceType;

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

    protected VDS getVds(Guid id) {
        return getVdsDao().get(id);
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

    protected boolean isCpuSupported(VM vm) {
        return VmHandler.isCpuSupported(
                vm.getVmOsId(),
                getVdsGroup().getCompatibilityVersion(),
                getVdsGroup().getCpuName(),
                getReturnValue().getCanDoActionMessages());
    }

    protected boolean setAndValidateCpuProfile() {
        return validate(CpuProfileHelper.setAndValidateCpuProfile(
                getParameters().getVm().getStaticData(),
                getVdsGroup().getCompatibilityVersion(),
                getUserId()));
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

            if (FeatureSupported.isMigrationSupported(getVdsGroup().getArchitecture(), getVdsGroup().getCompatibilityVersion())) {
                vmStatic.setMigrationSupport(instanceType.getMigrationSupport());
            }

            if (FeatureSupported.isIoThreadsSupported(getVdsGroup().getCompatibilityVersion())) {
                vmStatic.setNumOfIoThreads(instanceType.getNumOfIoThreads());
            }

            vmStatic.setMigrationDowntime(instanceType.getMigrationDowntime());
            vmStatic.setPriority(instanceType.getPriority());
            vmStatic.setTunnelMigration(instanceType.getTunnelMigration());

            List<VmDevice> vmDevices = VmDeviceUtils.getMemoryBalloons(instanceType.getId());
            vmStatic.setMinAllocatedMem(instanceType.getMinAllocatedMem());
            if (vmDevices.isEmpty()) {
                getParameters().setBalloonEnabled(false);
            } else if (osRepository.isBalloonEnabled(getParameters().getVmStaticData().getOsId(), getVdsGroup().getCompatibilityVersion())) {
                getParameters().setBalloonEnabled(true);
            }
        }
    }
}
