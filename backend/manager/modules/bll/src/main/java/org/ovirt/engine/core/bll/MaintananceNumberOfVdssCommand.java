package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MaintananceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.MaintananceVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class MaintananceNumberOfVdssCommand<T extends MaintananceNumberOfVdssParameters> extends CommandBase<T> {
    private static final long serialVersionUID = -5691756067222085075L;
    private final HashMap<Guid, VDS> vdssToMaintenance = new HashMap<Guid, VDS>();
    private ArrayList<Guid> _vdsGroupIds;
    private final List<PermissionSubject> inspectedEntitiesMap;
    private Map<String, String> sharedLockMap;

    public MaintananceNumberOfVdssCommand(T parameters) {
        super(parameters);
        Iterable<Guid> vdsIdList = getParameters().getVdsIdList();
        inspectedEntitiesMap = new ArrayList<PermissionSubject>();
        for (Guid g : vdsIdList) {
            inspectedEntitiesMap.add(new PermissionSubject(g,
                    VdcObjectType.VDS,
                    getActionType().getActionGroup()));
        }
    }

    private void MoveVdssToGoingToMaintananceMode() {
        List<VDS> spms = new ArrayList<VDS>();
        for (VDS vds : vdssToMaintenance.values()) {
            if (vds.getspm_status() != VdsSpmStatus.SPM) {
                setVdsStatusToPrepareForMaintaice(vds);
            } else {
                spms.add(vds);
            }
        }
        for (VDS vds : spms) {
            setVdsStatusToPrepareForMaintaice(vds);
        }
        freeLock();
    }

    private void setVdsStatusToPrepareForMaintaice(VDS vds) {
        if (vds.getstatus() != VDSStatus.PreparingForMaintenance && vds.getstatus() != VDSStatus.NonResponsive
                && vds.getstatus() != VDSStatus.Down) {
            runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(vds.getId(), VDSStatus.PreparingForMaintenance));
        }
    }

    private void MigrateAllVdss() {
        for (Guid vdsId : getParameters().getVdsIdList()) {
            // ParametersCurrentUser = CurrentUser
            MaintananceVdsParameters tempVar = new MaintananceVdsParameters(vdsId, getParameters().getIsInternal());
            tempVar.setSessionId(getParameters().getSessionId());
            tempVar.setCorrelationId(getParameters().getCorrelationId());
            VdcReturnValueBase result =
                    Backend.getInstance().runInternalAction(VdcActionType.MaintananceVds,
                            tempVar,
                            ExecutionHandler.createInternalJobContext());
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
        MoveVdssToGoingToMaintananceMode();
        MigrateAllVdss();
        // set network to operational / non-operational
        for (Guid id : _vdsGroupIds) {
            List<Network> networks = DbFacade.getInstance().getNetworkDao().getAllForCluster(id);
            for (Network net : networks) {
                NetworkClusterHelper.setStatus(id, net);
            }
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        _vdsGroupIds = new ArrayList<Guid>();
        Set<Guid> vdsWithRunningVMs = new HashSet<Guid>();
        List<String> hostNotRespondingList = new ArrayList<String>();
        List<String> hostsWithNonMigratableVms = new ArrayList<String>();
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
                if (vds.getspm_status() == VdsSpmStatus.SPM) {
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
                    if (vms.size() > 0) {
                        vdsWithRunningVMs.add(vdsId);
                    }
                    _vdsGroupIds.add(vds.getvds_group_id());
                    List<String> nonMigratableVmDescriptionsToFrontEnd = new ArrayList<String>();
                    for (VM vm : vms) {
                        if (vm.getMigrationSupport() != MigrationSupport.MIGRATABLE) {
                            nonMigratableVmDescriptionsToFrontEnd.add(vm.getVmName());
                        }
                    }

                    if (nonMigratableVmDescriptionsToFrontEnd.size() > 0) {
                        hostsWithNonMigratableVms.add(vds.getvds_name());
                        nonMigratableVms.addAll(nonMigratableVmDescriptionsToFrontEnd);

                        // The non migratable VM names will be comma separated
                        log.error(String.format("VDS %1$s contains non migratable VMs", vdsId));
                        result = false;

                    } else if (vds.getstatus() == VDSStatus.Maintenance) {
                        addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_IN_MAINTENANCE);
                        result = false;
                    } else if (vds.getspm_status() == VdsSpmStatus.Contending) {
                        addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_SPM_CONTENDING);
                        result = false;
                    } else if (vds.getstatus() == VDSStatus.NonResponsive && vds.getvm_count() > 0) {
                        result = false;
                        hostNotRespondingList.add(vds.getvds_name());
                    } else if (vds.getstatus() == VDSStatus.NonResponsive && vds.getspm_status() != VdsSpmStatus.None) {
                        result = false;
                        addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_AND_SPM);
                    } else if (vds.getspm_status() == VdsSpmStatus.SPM && vds.getstatus() == VDSStatus.Up) {
                        try {
                            @SuppressWarnings("unchecked")
                            HashMap<Guid, AsyncTaskStatus> taskStatuses =
                                    (HashMap<Guid, AsyncTaskStatus>) Backend
                                            .getInstance()
                                            .getResourceManager()
                                            .RunVdsCommand(VDSCommandType.HSMGetAllTasksStatuses,
                                                    new VdsIdVDSCommandParametersBase(vdsId)).getReturnValue();
                            if (taskStatuses.size() > 0) {
                                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_SPM_WITH_RUNNING_TASKS);
                                result = false;
                            }
                        } catch (VdcBLLException e) {
                            if (e.getErrorCode() == VdcBllErrors.VDS_NETWORK_ERROR) {
                                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_AND_SPM);
                                result = false;
                            } else {
                                log.error("Error getting spm task list.", e);
                            }
                        } catch (RuntimeException exp) {
                            log.error("Error getting spm task list.", exp);
                        }
                    }
                }
            }

            // If one of the host is non responsive with running VM's, add a CanDoAction message.
            handleNonResponsiveHosts(hostNotRespondingList);

            // If one of the vms is non migratable, add a CanDoAction message.
            handleNonMigratableVms(hostsWithNonMigratableVms, nonMigratableVms);

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
                Set<Guid> clustersAsSet = new HashSet<Guid>();
                clustersAsSet.addAll(_vdsGroupIds);
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
                                candidateHostsWithRunningVms.add(vdsInCluster.getvds_name());
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
                    addCanDoActionMessage(VdcBllMessages.CANNOT_MAINTANANCE_VDS_RUN_VMS_NO_OTHER_RUNNING_VDS);
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
            sharedLockMap = new HashMap<String, String>();
        }
        sharedLockMap.put(vds.getStoragePoolId().toString(), LockingGroup.POOL.name());
    }

    /**
     * If found hosts which has non-migratable VM's on them, add the host names and Vm's to the message.
     *
     * @param hostNotRespondingList
     *            - List of non responsive hosts with running VM's on them.
     */
    private void handleNonMigratableVms(List<String> hostsWithNonMigratableVms, List<String> nonMigratableVms) {
        if (!hostsWithNonMigratableVms.isEmpty()) {
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
            if (vds.getstatus() == VDSStatus.Up) {
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
     * If found hosts which are non-responsive and has VM's on them, add the host names to the message.
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

    private void addClusterDetails(Guid vdsGroupID, List<String> clustersWithRunningVms) {
        if (vdsGroupID != null && !vdsGroupID.equals(Guid.Empty)) {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().getWithRunningVms(vdsGroupID);
            if (vdsGroup != null) {
                clustersWithRunningVms.add(vdsGroup.getname());
            }
        }
    }

    @Override
    protected Map<String, String> getSharedLocks() {
        return sharedLockMap;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.unmodifiableList(inspectedEntitiesMap);
    }
}
