package org.ovirt.engine.core.bll;

import java.text.MessageFormat;
import java.util.List;

import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class UpdateVdsGroupCommand<T extends VdsGroupOperationParameters> extends
VdsGroupOperationCommandBase<T> {
    public UpdateVdsGroupCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        // YAIRPOSTGRES - this code should be split to several blocks of run in new transaction + set states
        VDSGroup oldGroup = DbFacade.getInstance().getVdsGroupDAO().get(
                getParameters().getVdsGroup().getID());
        CheckMaxMemoryOverCommitValue();
        DbFacade.getInstance().getVdsGroupDAO().update(getParameters().getVdsGroup());

        if (oldGroup.getstorage_pool_id() != null
                && !oldGroup.getstorage_pool_id().equals(getVdsGroup().getstorage_pool_id())
                || oldGroup.getstorage_pool_id() == null
                && getVdsGroup().getstorage_pool_id() != null) {
            for (VdsStatic vds : DbFacade.getInstance().getVdsStaticDAO().getAllForVdsGroup(oldGroup.getID())) {
                VdsActionParameters parameters = new VdsActionParameters(vds.getId());
                if (oldGroup.getstorage_pool_id() != null) {
                    VdcReturnValueBase removeVdsSpmIdReturn =
                            Backend.getInstance().runInternalAction(VdcActionType.RemoveVdsSpmId,
                                    parameters);
                    if (!removeVdsSpmIdReturn.getSucceeded()) {
                        setSucceeded(false);
                        getReturnValue().setFault(removeVdsSpmIdReturn.getFault());
                        return;
                    }
                }
                if (getVdsGroup().getstorage_pool_id() != null) {
                    VdcReturnValueBase addVdsSpmIdReturn =
                            Backend.getInstance().runInternalAction(VdcActionType.AddVdsSpmId, parameters);
                    if (!addVdsSpmIdReturn.getSucceeded()) {
                        setSucceeded(false);
                        getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                        return;
                    }
                }
            }
        }

        // when changing data center we check that default networks exists in
        // cluster
        List<network> networks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(getVdsGroup().getID());
        boolean exists = false;
        String managementNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
        for (network net : networks) {
            if (StringHelper.EqOp(net.getname(), managementNetwork)) {
                exists = true;
            }
        }
        if (!exists) {
            if (getVdsGroup().getstorage_pool_id() != null) {
                List<network> storagePoolNets = DbFacade
                            .getInstance()
                            .getNetworkDAO()
                            .getAllForDataCenter(
                                    getVdsGroup().getstorage_pool_id()
                                            .getValue());
                for (network net : storagePoolNets) {
                    if (StringHelper.EqOp(net.getname(), managementNetwork)) {
                        DbFacade.getInstance().getNetworkClusterDAO().save(
                                    new network_cluster(getVdsGroup().getID(), net.getId(),
                                            NetworkStatus.Operational.getValue(), true));
                    }
                }
            }
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().getIsInternalCommand()) {
            return getSucceeded() ? AuditLogType.SYSTEM_UPDATE_VDS_GROUP
                    : AuditLogType.SYSTEM_UPDATE_VDS_GROUP_FAILED;
        } else {
            return getSucceeded() ? AuditLogType.USER_UPDATE_VDS_GROUP
                    : AuditLogType.USER_UPDATE_VDS_GROUP_FAILED;
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean result = super.canDoAction();
        getReturnValue().getCanDoActionMessages()
                .add(VdcBllMessages.VAR__ACTION__UPDATE.toString());
        VDSGroup oldGroup = DbFacade.getInstance().getVdsGroupDAO().get(getVdsGroup().getID());
        // check that if name was changed, it was done to the same cluster
        VDSGroup groupWithName = DbFacade.getInstance().getVdsGroupDAO().getByName(
                getVdsGroup().getname());
        if (oldGroup != null && !StringHelper.EqOp(oldGroup.getname(), getVdsGroup().getname())) {
            if (groupWithName != null && !groupWithName.getID().equals(getVdsGroup().getID())) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DO_ACTION_NAME_IN_USE);
                result = false;
            }
        }
        if (oldGroup == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            result = false;
        }
        // If both original Cpu and new Cpu are null, don't check Cpu validity
        if (result && (oldGroup.getcpu_name() != null || getVdsGroup().getcpu_name() != null)) {
            // Check that cpu exist
            if (!CpuFlagsManagerHandler.CheckIfCpusExist(getVdsGroup().getcpu_name(), getVdsGroup()
                    .getcompatibility_version())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CPU_NOT_FOUND);

                addCanDoActionMessage(VdcBllMessages.VAR__TYPE__CLUSTER);
                result = false;
            } else {
                // if cpu changed from intel to amd (or backwards) and there are
                // vds in this cluster, cannot update
                if (!StringHelper.isNullOrEmpty(oldGroup.getcpu_name())
                        && !CpuFlagsManagerHandler.CheckIfCpusSameManufacture(oldGroup
                                .getcpu_name(), getVdsGroup().getcpu_name(), getVdsGroup()
                                .getcompatibility_version())
                        && DbFacade.getInstance().getVdsStaticDAO().getAllForVdsGroup(getVdsGroup().getID())
                                .size() > 0) {
                    addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_ILLEGAL);
                    result = false;
                }
            }
        }
        if (result && !VersionSupport.checkVersionSupported(getVdsGroup()
                .getcompatibility_version())) {
            addCanDoActionMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        }
        // decreasing of compatibility version is not allowed
        if (result && getVdsGroup().getcompatibility_version().compareTo(
                oldGroup.getcompatibility_version()) < 0) {
            result = false;
            getReturnValue()
            .getCanDoActionMessages()
            .add(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION
                    .toString());
        }
        if (result) {
            SearchParameters p = new SearchParameters(MessageFormat.format(
                    StorageHandlingCommandBase.UpVdssInCluster, oldGroup.getname()),
                    SearchType.VDS);
            p.setMaxCount(Integer.MAX_VALUE);
            Iterable<VDS> clusterUpVdss = (Iterable<VDS>) Backend.getInstance()
            .runInternalQuery(VdcQueryType.Search, p).getReturnValue();
            for (VDS vds : clusterUpVdss) {
                if (!VersionSupport.checkClusterVersionSupported(
                        getVdsGroup().getcompatibility_version(), vds)) {
                    result = false;
                    getReturnValue()
                    .getCanDoActionMessages()
                    .add(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_LOWER_HOSTS
                            .toString());
                    break;
                } else if (CpuFlagsManagerHandler.missingServerCpuFlags(getVdsGroup()
                        .getcpu_name(), vds.getcpu_flags(), getVdsGroup()
                        .getcompatibility_version()) != null) {
                    getReturnValue().getCanDoActionMessages().add(
                            VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_WITH_LOWER_HOSTS
                            .toString());
                    result = false;
                    break;
                }
            }
        }

        if (result && (oldGroup.getstorage_pool_id() != null
                && !oldGroup.getstorage_pool_id().equals(getVdsGroup().getstorage_pool_id()))) {
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_CHANGE_STORAGE_POOL);
            result = false;
        }
        if (result) {
            SearchParameters searchParams = new SearchParameters("vms: cluster = "
                    + oldGroup.getname(), SearchType.VM);
            searchParams.setMaxCount(Integer.MAX_VALUE);

            List<VM> vmList = (List) Backend.getInstance()
            .runInternalQuery(VdcQueryType.Search, searchParams).getReturnValue();
            int notDownVms = 0;
            int suspendedVms = 0;
            for (VM vm : vmList) {
                // the search can return vm from cluster with similar name
                // so it's critical to check that
                // the vm cluster id is the same as the cluster.id
                if (!vm.getvds_group_id().equals(oldGroup.getID())) {
                    continue;
                }
                VMStatus vmStatus = vm.getstatus();
                if (vmStatus == VMStatus.Suspended) {
                    suspendedVms++;
                }
                if (vmStatus != VMStatus.Down) {
                    notDownVms++;
                }
            }
            if (notDownVms > 0
                    && !oldGroup.getcompatibility_version().equals(getVdsGroup().getcompatibility_version())) {
                result = false;
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_COMPATIBILITY_VERSION_WITH_RUNNING_VMS);
            }
            boolean sameCpuNames = StringHelper.EqOp(oldGroup.getcpu_name(), getVdsGroup().getcpu_name());
            if (result && !sameCpuNames) {
                if (suspendedVms > 0) {
                    addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_UPDATE_CPU_WITH_SUSPENDED_VMS);
                    result = false;
                } else if (notDownVms > 0) {
                    int compareResult =
                        CpuFlagsManagerHandler.compareCpuLevels(getVdsGroup().getcpu_name(),
                                oldGroup.getcpu_name(),
                                oldGroup.getcompatibility_version());
                    if (compareResult < 0) {
                        addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_LOWER_CPU_LEVEL);
                        result = false;
                    } else if (compareResult > 0) {// Upgrade of CPU in same compability level is allowed if there
                        // are running VMs - but we should warn they
                        // cannot not be hibernated
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.AddCustomValue("VdsGroup", getParameters().getVdsGroup().getname());
                        AuditLogDirector.log(logable,
                                AuditLogType.CANNOT_HIBERNATE_RUNNING_VMS_AFTER_CLUSTER_CPU_UPGRADE);
                    }
                }
            }
        }
        if (result && getVdsGroup().getstorage_pool_id() != null) {
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(
                    getVdsGroup().getstorage_pool_id().getValue());
            if (oldGroup.getstorage_pool_id() == null && storagePool.getstorage_pool_type() == StorageType.LOCALFS) {
                // we allow only one cluster in localfs data center
                if (!DbFacade
                        .getInstance()
                        .getVdsGroupDAO().getAllForStoragePool(
                                getVdsGroup().getstorage_pool_id().getValue()).isEmpty()) {
                    getReturnValue()
                    .getCanDoActionMessages()
                    .add(VdcBllMessages.VDS_GROUP_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE
                            .toString());
                    result = false;
                }
                // selection algorithm must be set to none in localfs
                else if (getVdsGroup().getselection_algorithm() != VdsSelectionAlgorithm.None) {
                    getReturnValue()
                    .getCanDoActionMessages()
                    .add(VdcBllMessages.VDS_GROUP_SELECTION_ALGORITHM_MUST_BE_SET_TO_NONE_ON_LOCAL_STORAGE
                            .toString());
                    result = false;
                }
                else if(VDSGroup.DEFAULT_VDS_GROUP_ID.equals(getVdsGroup().getID())) {
                    addCanDoActionMessage(VdcBllMessages.DEFAULT_CLUSTER_CANNOT_BE_ON_LOCALFS);
                    result = false;
                }
            }
        }
        if (result) {
            result = validateMetrics();
        }
        return result;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}
