package org.ovirt.engine.core.bll;

import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.numa.vm.NumaValidator;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public abstract class VmManagementCommandBase<T extends VmManagementParametersBase> extends VmCommand<T> {

    /**
     * This is the maximum value we can pass to cpuShares according to
     * the virsh man page.
     */
    public static final int MAXIMUM_CPU_SHARES = 262144;


    @Inject
    private NumaValidator numaValidator;
    @Inject
    private CpuProfileHelper cpuProfileHelper;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VdsDao vdsDao;

    private InstanceType instanceType;
    private Version effectiveCompatibilityVersion;

    protected VmManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmManagementCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        if (parameters.getVmStaticData() != null) {
            setVmId(parameters.getVmStaticData().getId());
            setClusterId(parameters.getVmStaticData().getClusterId());
        }
    }

    @Override
    protected void init() {
        super.init();
        initEffectiveCompatibilityVersion();
    }

    protected void initEffectiveCompatibilityVersion() {
        setEffectiveCompatibilityVersion(
                CompatibilityVersionUtils.getEffective(getParameters().getVmStaticData(), this::getCluster));
    }

    protected Guid getInstanceTypeId() {
        if (getParameters().getVmStaticData() != null) {
            return getParameters().getVmStaticData().getInstanceTypeId();
        }
        return null;
    }

    protected InstanceType getInstanceType() {
        if (instanceType == null && getInstanceTypeId() != null) {
            instanceType = vmTemplateDao.getInstanceType(getInstanceTypeId());
        }
        return instanceType;
    }

    protected Version getEffectiveCompatibilityVersion() {
        return effectiveCompatibilityVersion;
    }

    protected void setEffectiveCompatibilityVersion(Version effectiveCompatibilityVersion) {
        this.effectiveCompatibilityVersion = effectiveCompatibilityVersion;
    }

    protected VDS getVds(Guid id) {
        return vdsDao.get(id);
    }

    protected boolean validateCustomProperties(VmStatic vmStaticFromParams) {
        return VmPropertiesUtils.getInstance().validateVmProperties(
                getEffectiveCompatibilityVersion(),
                vmStaticFromParams.getCustomProperties(),
                getReturnValue().getValidationMessages());
    }

    protected boolean isVmWithSameNameExists(VmStatic vm, Guid storagePoolId) {
        return vmHandler.isVmWithSameNameExistStatic(vm, storagePoolId);
    }

    protected boolean isCpuSharesValid(VM vmData) {
        return vmData.getCpuShares() >= 0 && vmData.getCpuShares() <= MAXIMUM_CPU_SHARES;
    }

    protected boolean setAndValidateCpuProfile() {
        return validate(cpuProfileHelper.setAndValidateCpuProfile(
                getParameters().getVm().getStaticData(),
                getUserIdIfExternal().orElse(null)));
    }

    protected boolean validateCPUHotplug(VmStatic vmStaticData) {
        if (getVm().isRunningOrPaused()) {
            // Can't set more CPUs than available on the host where VM is running.
            // Potential overcommit (interference with other running VMs) will be resolved by scheduler.
            // In alignment with the CPUPolicyUnit, VM's hyperthreading is not considered.
            if (getVds() != null && vmStaticData.getNumOfCpus(false) > getVds().getCpuThreads()) {
                return false;
            }
        }

        return true;
    }

    protected boolean validateMemoryAlignment(VmStatic vmStaticData) {
        if (getCluster().getArchitecture().getFamily() == ArchitectureType.ppc && vmStaticData.getMemSizeMb() % 256 != 0) {
            return failValidation(EngineMessage.MEMORY_SIZE_NOT_MULTIPLE_OF_256_ON_PPC,
                    String.format("$%s %s", "clusterArch", getCluster().getArchitecture()));
        }
        return true;
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

            if (FeatureSupported.isMigrationSupported(getCluster().getArchitecture(), getEffectiveCompatibilityVersion())) {
                vmStatic.setMigrationSupport(instanceType.getMigrationSupport());
            }

            vmStatic.setNumOfIoThreads(instanceType.getNumOfIoThreads());
            vmStatic.setMigrationDowntime(instanceType.getMigrationDowntime());
            vmStatic.setPriority(instanceType.getPriority());
            vmStatic.setTunnelMigration(instanceType.getTunnelMigration());
            vmStatic.setMinAllocatedMem(instanceType.getMinAllocatedMem());

            vmStatic.setMigrationPolicyId(instanceType.getMigrationPolicyId());

        }
    }

    protected boolean canAddLease() {
        if (!getParameters().getVmStaticData().isAutoStartup()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_LEASES_ARE_NOT_SUPPORTED_WITH_HA_OFF);
        }
        return validateLeaseStorageDomain(getParameters().getVmStaticData().getLeaseStorageDomainId());
    }

    protected NumaValidator getNumaValidator() {
        return numaValidator;
    }

    protected static boolean isCompatibilityVersionSupportedByCluster(Version customCompatibilityVersion) {
        return Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels).contains(customCompatibilityVersion);
    }
}
