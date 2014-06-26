package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.MaintenanceVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CancelMigrationVDSParameters;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class MaintenanceNumberOfVdssCommand<T extends MaintenanceNumberOfVdssParameters> extends CommandBase<T> {
    private final HashMap<Guid, VDS> vdssToMaintenance = new HashMap<Guid, VDS>();
    private final List<PermissionSubject> inspectedEntitiesMap;
    private Map<String, Pair<String, String>> sharedLockMap;

    public MaintenanceNumberOfVdssCommand(T parameters) {
        this(parameters, null);
    }

    public MaintenanceNumberOfVdssCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        Iterable<Guid> vdsIdList = getParameters().getVdsIdList();
        inspectedEntitiesMap = new ArrayList<PermissionSubject>();
        for (Guid g : vdsIdList) {
            inspectedEntitiesMap.add(new PermissionSubject(g,
                    VdcObjectType.VDS,
                    getActionType().getActionGroup()));
        }
    }

    private void MoveVdssToGoingToMaintenanceMode() {
        List<VDS> spms = new ArrayList<VDS>();
        Iterator<VDS> it = vdssToMaintenance.values().iterator();
        while (it.hasNext()) {
            VDS vds = it.next();
            // SPMs will move to Prepare For Maintenance later after standard hosts
            if (vds.getSpmStatus() != VdsSpmStatus.SPM) {
                if (!setVdsStatusToPrepareForMaintenance(vds)) {
                    it.remove();
                }
            } else {
                spms.add(vds);
            }
        }
        for (VDS vds : spms) {
            if (!setVdsStatusToPrepareForMaintenance(vds)) {
                vdssToMaintenance.remove(vds.getId());
            }
        }

        cancelIncommingMigrations();
        freeLock();
    }

    private void cancelIncommingMigrations() {
        for (Guid hostId :vdssToMaintenance.keySet()) {
            for (VM vm : getVmDAO().getAllMigratingToHost(hostId)) {
                if (vm.getStatus() == VMStatus.MigratingFrom) {
                    log.infoFormat("Cancelling incoming migration of {0} id: {1}", vm, vm.getId());
                    runVdsCommand(VDSCommandType.CancelMigrate, new CancelMigrationVDSParameters(vm.getRunOnVds(), vm.getId(), true));
                }
            }
        }
    }

    private boolean setVdsStatusToPrepareForMaintenance(VDS vds) {
        boolean result = true;
        if (vds.getStatus() != VDSStatus.PreparingForMaintenance && vds.getStatus() != VDSStatus.NonResponsive
                && vds.getStatus() != VDSStatus.Down) {
            SetVdsStatusVDSCommandParameters params =
                    new SetVdsStatusVDSCommandParameters(vds.getId(), VDSStatus.PreparingForMaintenance);
            params.setStopSpmFailureLogged(true);
            result = runVdsCommand(VDSCommandType.SetVdsStatus, params).getSucceeded();
        }
        return result;
    }

    private void MigrateAllVdss() {
        for (Guid vdsId : vdssToMaintenance.keySet()) {
            // ParametersCurrentUser = CurrentUser
            MaintenanceVdsParameters tempVar = new MaintenanceVdsParameters(vdsId, getParameters().getIsInternal());
            tempVar.setSessionId(getParameters().getSessionId());
            tempVar.setCorrelationId(getParameters().getCorrelationId());
            VdcReturnValueBase result =
                    runInternalAction(VdcActionType.MaintenanceVds,
                            tempVar,
                            ExecutionHandler.createInternalJobContext(getContext()));
            if (!result.getCanDoAction()) {
                getReturnValue().getCanDoActionMessages().addAll(result.getCanDoActionMessages());
                getReturnValue().setCanDoAction(false);
            }
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MAINTENANCE);
    }

    @Override
    protected void executeCommand() {
        MoveVdssToGoingToMaintenanceMode();
        MigrateAllVdss();

        // find clusters for hosts that should move to maintenance
        Set<Guid> clusters = new HashSet<>();
        for (VDS vds : vdssToMaintenance.values()) {
            if (!clusters.contains(vds.getVdsGroupId())) {
                clusters.add(vds.getVdsGroupId());
                // set network to operational / non-operational
                List<Network> networks = DbFacade.getInstance().getNetworkDao().getAllForCluster(vds.getVdsGroupId());
                for (Network net : networks) {
                    NetworkClusterHelper.setStatus(vds.getVdsGroupId(), net);
                }
            }
        }

        // clear the automatic PM flag unless instructed otherwise
        if (!getParameters().getKeepPolicyPMEnabled()) {
            for (Guid vdsId : getParameters().getVdsIdList()) {
                getDbFacade().getVdsDynamicDao().updateVdsDynamicPowerManagementPolicyFlag(
                        vdsId,
                        false);
            }
        }

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        Set<Guid> clustersAsSet = new HashSet<Guid>();
        Set<Guid> vdsWithRunningVMs = new HashSet<Guid>();
        List<String> hostNotRespondingList = new ArrayList<String>();
        List<String> hostsWithNonMigratableVms = new ArrayList<String>();
        List<String> hostsWithVmsWithPluggedDiskSnapshots = new ArrayList<>();
        List<String> nonMigratableVms = new ArrayList<String>();

        for (Guid vdsId : getParameters().getVdsIdList()) {
            VDS vds = DbFacade.getInstance().getVdsDao().get(vdsId);
            if (vds == null) {
                log.error(String.format("ResourceManager::vdsMaintenance could not find vds_id = '%1$s'", vdsId));
                addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
                result = false;
                continue;
            }
            if (!vdssToMaintenance.containsKey(vdsId)) {
                vdssToMaintenance.put(vdsId, vds);
                if (vds.getSpmStatus() == VdsSpmStatus.SPM) {
                    addSharedLockEntry(vds);
                }
            }
        }
        result = result && acquireLockInternal();
        if (result) {
            // check if one of the target vdss is spm, if so check that there are no
            // tasks running
            for (Guid vdsId : getParameters().getVdsIdList()) {
                VDS vds = vdssToMaintenance.get(vdsId);
                if (vds != null) {
                    List<VM> vms = getVmDAO().getAllRunningForVds(vdsId);
                    if ((vds.getStatus() != VDSStatus.Maintenance) && (vds.getStatus() != VDSStatus.NonResponsive)
                            && (vds.getStatus() != VDSStatus.Up) && (vds.getStatus() != VDSStatus.Error)
                            && (vds.getStatus() != VDSStatus.PreparingForMaintenance)
                            && (vds.getStatus() != VDSStatus.Down)
                            && (vds.getStatus() != VDSStatus.NonOperational)) {
                        result = false;
                        addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_OPERATIONAL);
                    }
                    else {
                        if (vms.size() > 0) {
                            vdsWithRunningVMs.add(vdsId);
                        }
                        clustersAsSet.add(vds.getVdsGroupId());

                        List<String> nonMigratableVmDescriptionsToFrontEnd = new ArrayList<String>();
                        for (VM vm : vms) {
                            // The Hosted Engine VM is migrated by the HA agent;
                            // other non-migratable VMs are reported
                            if (vm.getMigrationSupport() != MigrationSupport.MIGRATABLE && !vm.isHostedEngine()) {
                                nonMigratableVmDescriptionsToFrontEnd.add(vm.getName());
                            }
                        }

                        if (nonMigratableVmDescriptionsToFrontEnd.size() > 0) {
                            hostsWithNonMigratableVms.add(vds.getName());
                            nonMigratableVms.addAll(nonMigratableVmDescriptionsToFrontEnd);

                            // The non migratable VM names will be comma separated
                            log.error(String.format("VDS %1$s contains non migratable VMs", vdsId));
                            result = false;

                        } else if (!validate(new VmValidator(vms)
                                .vmNotHavingPluggedDiskSnapshots(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VM_HAS_PLUGGED_DISK_SNAPSHOT))) {
                            hostsWithVmsWithPluggedDiskSnapshots.add(vds.getName());
                            result = false;
                        } else if (vds.getStatus() == VDSStatus.Maintenance) {
                            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_IN_MAINTENANCE);
                            result = false;
                        } else if (vds.getSpmStatus() == VdsSpmStatus.Contending) {
                            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_SPM_CONTENDING);
                            result = false;
                        } else if (vds.getStatus() == VDSStatus.NonResponsive && vds.getVmCount() > 0) {
                            result = false;
                            hostNotRespondingList.add(vds.getName());
                        } else if (vds.getStatus() == VDSStatus.NonResponsive
                                && vds.getSpmStatus() != VdsSpmStatus.None) {
                            result = false;
                            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_AND_IS_SPM);
                        } else if (vds.getSpmStatus() == VdsSpmStatus.SPM && vds.getStatus() == VDSStatus.Up &&
                                getAsyncTaskDao().getAsyncTaskIdsByStoragePoolId(vds.getStoragePoolId()).size() > 0) {
                            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_SPM_WITH_RUNNING_TASKS);
                            result = false;
                        }
                    }
                }
            }

            // If one of the host is non responsive with running VM's, add a CanDoAction message.
            handleNonResponsiveHosts(hostNotRespondingList);

            // If one of the vms is non migratable, add a CanDoAction message.
            handleNonMigratableVms(hostsWithNonMigratableVms, nonMigratableVms);

            handleHostsWithVmsWithPluggedDiskSnapshots(hostsWithVmsWithPluggedDiskSnapshots);

            if (result) {
                // Remove all redundant clusters in clusters list, by adding it to a
                // set.
                // For each cluster check for each host that belongs to it, if its a
                // part of the parameters and
                // if there are running hosts for it - if it is up and is not in the
                // parameters -migration will be possible
                // to be performed, and there is no point to continue the check for
                // the given cluster - otherwise,
                // if the host is up and in the parameters - it may be that the
                // cluster is problematic (no hosts in up
                // state that we will be able to migrate VMs to)

                // In the end - if the clusters list is not empty - this is an
                // error, use the "problematic clusters list" to format an error to
                // the client
                List<String> problematicClusters = new ArrayList<String>();
                List<String> allHostsWithRunningVms = new ArrayList<String>();
                for (Guid clusterID : clustersAsSet) {
                    List<VDS> vdsList = DbFacade.getInstance().getVdsDao().getAllForVdsGroup(clusterID);
                    boolean vdsForMigrationExists =
                            checkIfThereIsVDSToHoldMigratedVMs(getParameters().getVdsIdList(), vdsList);

                    if (!vdsForMigrationExists) {
                        List<String> candidateHostsWithRunningVms = new ArrayList<String>();
                        for (VDS vdsInCluster : vdsList) {
                            if (vdsWithRunningVMs.contains(vdsInCluster.getId())) {
                                candidateHostsWithRunningVms.add(vdsInCluster.getName());
                            }
                        }
                        // Passed on all vds in cluster - if map is not empty (host found with VMs) -
                        // this is indeed a problematic
                        // cluster
                        if (!candidateHostsWithRunningVms.isEmpty()) {
                            addClusterDetails(clusterID, problematicClusters);
                            allHostsWithRunningVms.addAll(candidateHostsWithRunningVms);
                        }
                    }
                }
                // If there are problematic clusters
                result = problematicClusters.isEmpty();
                if (!result) {
                    addCanDoActionMessage(VdcBllMessages.CANNOT_MAINTENANCE_VDS_RUN_VMS_NO_OTHER_RUNNING_VDS);
                    String commaDelimitedClusters = StringUtils.join(problematicClusters, ",");
                    getReturnValue().getCanDoActionMessages().add(String.format("$ClustersList %1$s",
                            commaDelimitedClusters));
                    getReturnValue().getCanDoActionMessages().add(String.format("$HostsList %1$s",
                            StringUtils.join(allHostsWithRunningVms, ",")));
                }
            }
        }
        return result;
    }

    private void addSharedLockEntry(VDS vds) {
        if (sharedLockMap == null) {
            sharedLockMap = new HashMap<String, Pair<String, String>>();
        }
        sharedLockMap.put(vds.getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    /**
     * If found hosts which has non-migratable VM's on them, add the host names and Vm's to the message.
     *
     * @param hostNotRespondingList
     *            - List of non responsive hosts with running VM's on them.
     */
    private void handleNonMigratableVms(List<String> hostsWithNonMigratableVms, List<String> nonMigratableVms) {
        if (!nonMigratableVms.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_IT_INCLUDES_NON_MIGRATABLE_VM);
            getReturnValue().getCanDoActionMessages().add((String.format("$VmsList %1$s",
                    StringUtils.join(nonMigratableVms, " , "))));
            getReturnValue().getCanDoActionMessages().add((String.format("$HostsList %1$s",
                    StringUtils.join(hostsWithNonMigratableVms, " , "))));
        }
    }

    private boolean checkIfThereIsVDSToHoldMigratedVMs(Iterable<Guid> vdsIDListFromParams, List<VDS> vdsInCluster) {
        // Checks if the number of UP VDS in the parameters for a given cluster
        // is
        // less than the number of UP parameters in that cluster - if this is
        // the case, we have
        // a VDS to migrate to
        Set<Guid> upVdsIDsInCluster = new HashSet<Guid>();
        for (VDS vds : vdsInCluster) {
            if (vds.getStatus() == VDSStatus.Up) {
                upVdsIDsInCluster.add(vds.getId());
            }
        }
        int numOfUpVDSInClusterAndParams = 0;
        for (Guid vdsID : vdsIDListFromParams) {
            if (upVdsIDsInCluster.contains(vdsID)) {
                numOfUpVDSInClusterAndParams++;
            }
        }
        return numOfUpVDSInClusterAndParams < upVdsIDsInCluster.size();
    }

    /**
     * If found hosts which are non responsive and has VM's on them, add the host names to the message.
     *
     * @param hostNotRespondingList
     *            - List of non responsive hosts with running VM's on them.
     */
    private void handleNonResponsiveHosts(List<String> hostNotRespondingList) {
        if (!hostNotRespondingList.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_WITH_VMS);
            getReturnValue().getCanDoActionMessages().add(String.format("$HostNotResponding %1$s",
                    StringUtils.join(hostNotRespondingList, ",")));
        }
    }

    private void handleHostsWithVmsWithPluggedDiskSnapshots(List<String> hostsWithVmsWithPluggedDiskSnapshots) {
        if (!hostsWithVmsWithPluggedDiskSnapshots.isEmpty()) {
            getReturnValue().getCanDoActionMessages().add((String.format("$HostsList %1$s",
                    StringUtils.join(hostsWithVmsWithPluggedDiskSnapshots, ","))));
        }
    }

    private void addClusterDetails(Guid vdsGroupID, List<String> clustersWithRunningVms) {
        if (vdsGroupID != null && !vdsGroupID.equals(Guid.Empty)) {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().getWithRunningVms(vdsGroupID);
            if (vdsGroup != null) {
                clustersWithRunningVms.add(vdsGroup.getName());
            }
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return sharedLockMap;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.unmodifiableList(inspectedEntitiesMap);
    }
}
