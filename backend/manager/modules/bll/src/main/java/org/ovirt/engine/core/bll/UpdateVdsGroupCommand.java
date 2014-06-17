package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public class UpdateVdsGroupCommand<T extends VdsGroupOperationParameters> extends
        VdsGroupOperationCommandBase<T>  implements RenamedEntityInfoProvider{

    private List<VDS> allForVdsGroup;
    private VDSGroup oldGroup;

    public UpdateVdsGroupCommand(T parameters) {
        this(parameters, null);
    }

    public UpdateVdsGroupCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    protected UpdateVdsGroupCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        getVdsGroup().setArchitecture(getArchitecture());

        // TODO: This code should be revisited and proper compensation logic should be introduced here
        checkMaxMemoryOverCommitValue();
        if (!Objects.equals(oldGroup.getcompatibility_version(), getParameters().getVdsGroup().getcompatibility_version())) {
            String emulatedMachine = null;
            // pick an UP host randomly - all should have latest compat version already if we passed the canDo.
            for (VDS vds : allForVdsGroup) {
                if (vds.getStatus() == VDSStatus.Up) {
                    emulatedMachine = ListUtils.firstMatch(
                            Config.<List<String>>getValue(ConfigValues.ClusterEmulatedMachines,
                                    getParameters().getVdsGroup().getcompatibility_version().getValue()), vds.getSupportedEmulatedMachines().split(","));
                    break;
                }
            }
            if (emulatedMachine == null) {
                getParameters().getVdsGroup().setDetectEmulatedMachine(true);
            } else {
                getParameters().getVdsGroup().setEmulatedMachine(emulatedMachine);
            }
        }
        else if (oldGroup.getArchitecture() != getVdsGroup().getArchitecture()) {
            // if architecture was changed, emulated machines must be updated when adding new host.
            // At this point the cluster is empty and have changed CPU name
            getParameters().getVdsGroup().setDetectEmulatedMachine(true);
            getParameters().getVdsGroup().setEmulatedMachine(null);
        }

        getVdsGroupDAO().update(getParameters().getVdsGroup());

        if (oldGroup.getStoragePoolId() == null && getVdsGroup().getStoragePoolId() != null) {
            for (VDS vds : allForVdsGroup) {
                VdsActionParameters parameters = new VdsActionParameters();
                parameters.setVdsId(vds.getId());
                VdcReturnValueBase addVdsSpmIdReturn =
                        getBackend().runInternalAction(VdcActionType.AddVdsSpmId, parameters);
                if (!addVdsSpmIdReturn.getSucceeded()) {
                    setSucceeded(false);
                    getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                    return;
                }
            }

            // when moving the cluster back into a DC, need to add its management network
            Network managementNetwork =
                    getNetworkDAO().getByNameAndDataCenter(NetworkUtils.getEngineNetwork(),
                            getVdsGroup().getStoragePoolId());
            getNetworkClusterDAO().save(new NetworkCluster(getVdsGroup().getId(),
                    managementNetwork.getId(),
                    NetworkStatus.OPERATIONAL,
                    true,
                    true,
                    true));
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().getIsInternalCommand()) {
            return getSucceeded() ? AuditLogType.SYSTEM_UPDATE_VDS_GROUP
                    : AuditLogType.SYSTEM_UPDATE_VDS_GROUP_FAILED;
        }

        return getSucceeded() ? AuditLogType.USER_UPDATE_VDS_GROUP
                : AuditLogType.USER_UPDATE_VDS_GROUP_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        boolean hasVms = false;
        boolean hasVmOrHost = false;
        boolean sameCpuNames = false;
        boolean allVdssInMaintenance = false;

        List<VM> vmList = null;

        oldGroup = getVdsGroupDAO().get(getVdsGroup().getId());
        if (oldGroup == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            result = false;
        }
        // if the name was changed then make sure the new name is unique
        if (result && !StringUtils.equals(oldGroup.getName(), getVdsGroup().getName())) {
            if (!isVdsGroupUnique(getVdsGroup().getName())) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE);
                result = false;
            }
        }
        if (result && !VersionSupport.checkVersionSupported(getVdsGroup()
                .getcompatibility_version())) {
            addCanDoActionMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        }

        if (result) {
            allForVdsGroup = getVdsDAO().getAllForVdsGroup(oldGroup.getId());
        }
        // decreasing of compatibility version is only allowed when no hosts exists, and not beneath the DC version
        if (result && getVdsGroup().getcompatibility_version().compareTo(oldGroup.getcompatibility_version()) < 0) {
            if (!allForVdsGroup.isEmpty()) {
                result = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
            }

            if (oldGroup.getStoragePoolId() != null) {
                StoragePool storagePool = getStoragePoolDAO().get(oldGroup.getStoragePoolId());
                if (storagePool != null && getVdsGroup().getcompatibility_version()
                    .compareTo(storagePool.getcompatibility_version()) < 0) {
                    result = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION_UNDER_DC);
                }
            }

        }
        if (result && oldGroup.getStoragePoolId() != null
                && !oldGroup.getStoragePoolId().equals(getVdsGroup().getStoragePoolId())) {
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_CHANGE_STORAGE_POOL);
            result = false;
        }
        // If both original Cpu and new Cpu are null, don't check Cpu validity
        if (result) {
            allVdssInMaintenance = areAllVdssInMaintenance(allForVdsGroup);
        }
        // Validate the cpu only if the cluster supports Virt
        if (result && getVdsGroup().supportsVirtService()
                && (oldGroup.getcpu_name() != null || getVdsGroup().getcpu_name() != null)) {
            // Check that cpu exist
            if (!checkIfCpusExist()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND);
                addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER);
                result = false;
            } else {
                // if cpu changed from intel to amd (or backwards) and there are
                // vds in this cluster, cannot update
                if (!StringUtils.isEmpty(oldGroup.getcpu_name())
                        && !checkIfCpusSameManufacture(oldGroup)
                        && !allVdssInMaintenance) {
                    addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_ILLEGAL);
                    result = false;
                }
            }
        }

        if (result) {
            vmList = getVmDAO().getAllForVdsGroup(oldGroup.getId());
            hasVmOrHost = !vmList.isEmpty() || !allForVdsGroup.isEmpty();
        }

        // cannot change the the processor architecture while there are attached hosts or VMs to the cluster
        if (result  && getVdsGroup().supportsVirtService()
                && !isArchitectureUpdatable()
                && hasVmOrHost) {
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_ARCHITECTURE_ILLEGAL);
            result = false;
        }

        if (result) {
            sameCpuNames = StringUtils.equals(oldGroup.getcpu_name(), getVdsGroup().getcpu_name());
        }

        if (result) {
            boolean isOldCPUEmpty = StringUtils.isEmpty(oldGroup.getcpu_name());

            if (!isOldCPUEmpty && !sameCpuNames && !isCpuUpdatable(oldGroup) && hasVmOrHost) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CPU_IS_NOT_UPDATABLE);
                result = false;
            }
        }

        if (result) {
            List<VDS> vdss = new ArrayList<VDS>();
            boolean isAddedToStoragePool = oldGroup.getStoragePoolId() == null
                    && getVdsGroup().getStoragePoolId() != null;
            for (VDS vds : allForVdsGroup) {
                if (vds.getStatus() == VDSStatus.Up) {
                    if (isAddedToStoragePool) {
                        addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_VDS_UP);
                        return false;
                    } else {
                        vdss.add(vds);
                    }
                }
            }
            for (VDS vds : vdss) {
                if (!VersionSupport.checkClusterVersionSupported(
                        getVdsGroup().getcompatibility_version(), vds)) {
                    result = false;
                    addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS);
                    break;
                } else if (getVdsGroup().supportsVirtService() && missingServerCpuFlags(vds) != null) {
                    addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS);
                    result = false;
                    break;
                }
            }
            if (result) {
                boolean notDownVms = false;
                boolean suspendedVms = false;
                hasVms = vmList.size() > 0;

                if (!sameCpuNames) {
                    for (VM vm : vmList) {
                        if (vm.getStatus() == VMStatus.Suspended) {
                            suspendedVms = true;
                            break;
                        } else if (vm.getStatus() != VMStatus.Down) {
                            notDownVms = true;
                            break;
                        }
                    }
                    if (suspendedVms) {
                        addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_WITH_SUSPENDED_VMS);
                        result = false;
                    } else if (notDownVms) {
                        int compareResult = compareCpuLevels(oldGroup);
                        if (compareResult > 0) {// Upgrade of CPU in same compability level is allowed if
                                                       // there
                            // are running VMs - but we should warn they
                            // cannot not be hibernated
                            AuditLogableBase logable = new AuditLogableBase();
                            logable.addCustomValue("VdsGroup", getParameters().getVdsGroup().getName());
                            AuditLogDirector.log(logable,
                                    AuditLogType.CANNOT_HIBERNATE_RUNNING_VMS_AFTER_CLUSTER_CPU_UPGRADE);
                        }
                    }
                }
            }
        }
        if (result && getVdsGroup().getStoragePoolId() != null) {
            StoragePool storagePool = getStoragePoolDAO().get(getVdsGroup().getStoragePoolId());
            if (oldGroup.getStoragePoolId() == null && storagePool.isLocal()) {
                // we allow only one cluster in localfs data center
                if (!getVdsGroupDAO().getAllForStoragePool(getVdsGroup().getStoragePoolId()).isEmpty()) {
                    getReturnValue()
                            .getCanDoActionMessages()
                            .add(VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE
                                    .toString());
                    result = false;
                }
                else if (Config.getValue(ConfigValues.AutoRegistrationDefaultVdsGroupID).equals(getVdsGroup().getId())) {
                    addCanDoActionMessage(VdcBllMessages.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
                    result = false;
                }
            }
        }

        if (getVdsGroup().getcompatibility_version() != null
                && Version.v3_3.compareTo(getVdsGroup().getcompatibility_version()) > 0
                && getVdsGroup().isEnableBallooning()) {
            // Members of pre-3.3 clusters don't support ballooning; here we act like a 3.2 engine
            addCanDoActionMessage(VdcBllMessages.QOS_BALLOON_NOT_SUPPORTED);
            result = false;
        }

        if (getVdsGroup().supportsGlusterService()
                && !GlusterFeatureSupported.gluster(getVdsGroup().getcompatibility_version())) {
            addCanDoActionMessage(VdcBllMessages.GLUSTER_NOT_SUPPORTED);
            addCanDoActionMessageVariable("compatibilityVersion", getVdsGroup().getcompatibility_version().getValue());
            result = false;
        }

        if (result) {
            if (!(getVdsGroup().supportsGlusterService() || getVdsGroup().supportsVirtService())) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_AT_LEAST_ONE_SERVICE_MUST_BE_ENABLED);
                result = false;
            }
            else if (getVdsGroup().supportsGlusterService() && getVdsGroup().supportsVirtService()
                    && !isAllowClusterWithVirtGluster()) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_ENABLING_BOTH_VIRT_AND_GLUSTER_SERVICES_NOT_ALLOWED);
                result = false;
            }
        }
        if (result && hasVms && !getVdsGroup().supportsVirtService()) {
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DISABLE_VIRT_WHEN_CLUSTER_CONTAINS_VMS);
            result = false;
        }
        if (result && !getVdsGroup().supportsGlusterService()) {
            List<GlusterVolumeEntity> volumes = getGlusterVolumeDao().getByClusterId(getVdsGroup().getId());
            if (volumes != null && volumes.size() > 0) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DISABLE_GLUSTER_WHEN_CLUSTER_CONTAINS_VOLUMES);
                result = false;
            }
        }
        if (result && getVdsGroup().supportsTrustedService() && Config.<String> getValue(ConfigValues.AttestationServer).equals("")) {
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_SET_TRUSTED_ATTESTATION_SERVER_NOT_CONFIGURED);
            result = false;
        }
        if (result) {
            result = validateClusterPolicy();
        }
        // non-empty required sources list and rng-unsupported cluster version
        if (result && !getVdsGroup().getRequiredRngSources().isEmpty()
                && !FeatureSupported.virtIoRngSupported(getVdsGroup().getcompatibility_version())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_RNG_SOURCE_NOT_SUPPORTED);
            result = false;
        }
        return result;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    protected boolean isArchitectureUpdatable() {
        return oldGroup.getArchitecture() == ArchitectureType.undefined ? true
                : getArchitecture() == oldGroup.getArchitecture();
    }

    protected boolean checkIfCpusSameManufacture(VDSGroup group) {
        return CpuFlagsManagerHandler.CheckIfCpusSameManufacture(group.getcpu_name(),
                getVdsGroup().getcpu_name(),
                getVdsGroup().getcompatibility_version());
    }

    protected boolean checkIfCpusExist() {
        return CpuFlagsManagerHandler.CheckIfCpusExist(getVdsGroup().getcpu_name(),
                getVdsGroup().getcompatibility_version());
    }

    protected List<String> missingServerCpuFlags(VDS vds) {
        return CpuFlagsManagerHandler.missingServerCpuFlags(
                getVdsGroup().getcpu_name(),
                vds.getCpuFlags(),
                getVdsGroup().getcompatibility_version());
    }

    protected boolean isCpuUpdatable(VDSGroup cluster) {
        return CpuFlagsManagerHandler.isCpuUpdatable(cluster.getcpu_name(), cluster.getcompatibility_version());
    }

    private boolean areAllVdssInMaintenance(List<VDS> vdss) {
        boolean allInMaintenance = true;
        for (VDS vds : vdss) {
            if (vds.getStatus() != VDSStatus.Maintenance) {
                allInMaintenance = false;
                break;
            }
        }
        return allInMaintenance;
    }

    protected int compareCpuLevels(VDSGroup otherGroup) {
        return CpuFlagsManagerHandler.compareCpuLevels(getVdsGroup().getcpu_name(),
                otherGroup.getcpu_name(),
                otherGroup.getcompatibility_version());
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    protected VdsStaticDAO getVdsStaticDAO() {
        return getDbFacade().getVdsStaticDao();
    }

    @Override
    protected NetworkDao getNetworkDAO() {
        return getDbFacade().getNetworkDao();
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.VdsGroups.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return oldGroup.getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getVdsGroup().getName();
    }

    @Override
    public void setEntityId(AuditLogableBase logable) {
        logable.setVdsGroupId(oldGroup.getId());
    }
}
