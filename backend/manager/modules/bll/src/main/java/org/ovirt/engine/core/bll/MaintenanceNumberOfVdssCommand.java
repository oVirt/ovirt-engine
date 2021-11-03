package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.gluster.GlusterHostValidator;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.MultipleVmsValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.MaintenanceVdsParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.vdsbroker.monitoring.PollVmStatsRefresher;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CancelMigrationVDSParameters;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class MaintenanceNumberOfVdssCommand<T extends MaintenanceNumberOfVdssParameters> extends CommandBase<T> {
    private final Map<Guid, VDS> vdssToMaintenance = new HashMap<>();
    private final List<PermissionSubject> inspectedEntitiesMap;
    private Map<String, Pair<String, String>> sharedLockMap;

    @Inject
    private GlusterHostValidator glusterHostValidator;
    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private AsyncTaskDao asyncTaskDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private StepDao stepDao;
    @Inject
    private ImageTransferDao imageTransferDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private AuditLogDirector auditLogDirector;

    public MaintenanceNumberOfVdssCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        Iterable<Guid> vdsIdList = getParameters().getVdsIdList();
        inspectedEntitiesMap = new ArrayList<>();
        for (Guid g : vdsIdList) {
            inspectedEntitiesMap.add(new PermissionSubject(g,
                    VdcObjectType.VDS,
                    getActionType().getActionGroup()));
        }
    }

    private void moveVdssToGoingToMaintenanceMode() {
        List<VDS> spms = new ArrayList<>();
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

        cancelIncomingMigrations();
        freeLock();
    }

    private void cancelIncomingMigrations() {
        boolean waitForGetAllVmStats = false;
        for (Guid hostId : vdssToMaintenance.keySet()) {
            for (VmDynamic vm : vmDynamicDao.getAllMigratingToHost(hostId)) {
                if (vm.getStatus() == VMStatus.MigratingFrom) {
                    log.info("Cancelling incoming migration of '{}' id '{}'", vm, vm.getId());
                    try {
                        runVdsCommand(VDSCommandType.CancelMigrate, new CancelMigrationVDSParameters(vm.getRunOnVds(), vm.getId(), true));
                    } catch (EngineException e) {
                        // The migration might end right before calling cancel, that will cause an exception within
                        // the CancelMigrate command. Although, it can be fine.
                        // We should wait 15 seconds to let the DB refresh and continue.
                        log.warn("Engine exception thrown while sending cancel migration command, {}", vm.getId());
                        if (e.getErrorCode() == EngineError.MIGRATION_CANCEL_ERROR_NO_VM
                                || e.getErrorCode() == EngineError.MIGRATION_CANCEL_ERROR) {
                            waitForGetAllVmStats = true;
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }
        if (waitForGetAllVmStats) {
            try {
                Thread.sleep(PollVmStatsRefresher.VMS_REFRESH_RATE * PollVmStatsRefresher.NUMBER_VMS_REFRESHES_BEFORE_SAVE);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    private boolean setVdsStatusToPrepareForMaintenance(VDS vds) {
        boolean result = true;
        if (vds.getStatus() != VDSStatus.PreparingForMaintenance && vds.getStatus() != VDSStatus.NonResponsive
                && vds.getStatus() != VDSStatus.Down) {
            SetVdsStatusVDSCommandParameters params =
                    new SetVdsStatusVDSCommandParameters(vds.getId(), VDSStatus.PreparingForMaintenance, getParameters().getReason());
            params.setStopSpmFailureLogged(true);
            result = runVdsCommand(VDSCommandType.SetVdsStatus, params).getSucceeded();
        }
        return result;
    }

    private boolean migrateAllVdss() {
        for (Guid vdsId : vdssToMaintenance.keySet()) {
            // ParametersCurrentUser = CurrentUser
            MaintenanceVdsParameters tempVar = new MaintenanceVdsParameters(vdsId,
                    getParameters().getIsInternal(),
                    getParameters().isStopGlusterService());
            tempVar.setSessionId(getParameters().getSessionId());
            tempVar.setCorrelationId(getParameters().getCorrelationId());
            tempVar.setParentCommand(getParameters().getCommandType());
            tempVar.setParentParameters(getParentParameters());
            ActionReturnValue result =
                    runInternalAction(ActionType.MaintenanceVds,
                            tempVar,
                            ExecutionHandler.createInternalJobContext(getContext()));
            if (!result.isValid()) {
                getReturnValue().getValidationMessages().addAll(result.getValidationMessages());
                getReturnValue().setValid(false);
            }
            if (!result.getSucceeded()) {
                return false;
            }
        }
        return true;
    }

    private void activateVdssFromGoingToMaintenanceMode() {
        for (Guid vdsId : vdssToMaintenance.keySet()) {
            VDS vds = vdsDao.get(vdsId);
            if (vds.getStatus() == VDSStatus.PreparingForMaintenance) {
                VdsActionParameters parameters = new VdsActionParameters(vds.getId());
                runInternalAction(ActionType.ActivateVds, parameters);
            }
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
        addValidationMessage(EngineMessage.VAR__ACTION__MAINTENANCE);
    }

    @Override
    protected void executeCommand() {
        moveVdssToGoingToMaintenanceMode();
        if (!migrateAllVdss()) {
            // If the migrate VMs failed and the command was invoked from UpgradeHost activate the hosts so that they
            // are not stuck in PreparingForMaintenance status.
            if (ActionType.UpgradeHost == getParameters().getParentCommand()) {
                activateVdssFromGoingToMaintenanceMode();
            }
            setSucceeded(false);
            return;
        }

        // find clusters for hosts that should move to maintenance
        Set<Guid> clusters = new HashSet<>();
        for (VDS vds : vdssToMaintenance.values()) {
            if (!clusters.contains(vds.getClusterId())) {
                clusters.add(vds.getClusterId());
                // set network to operational / non-operational
                List<Network> networks = networkDao.getAllForCluster(vds.getClusterId());
                networkClusterHelper.setStatus(vds.getClusterId(), networks);
            }
        }

        // clear the automatic PM flag unless instructed otherwise
        if (!getParameters().getKeepPolicyPMEnabled()) {
            for (Guid vdsId : getParameters().getVdsIdList()) {
                vdsDynamicDao.updateVdsDynamicPowerManagementPolicyFlag(vdsId, false);
            }
        }

        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        boolean result = true;
        Map<Guid, Cluster> clusters = new HashMap<>();
        Set<Guid> clustersAsSet = new HashSet<>();
        Set<Guid> vdsWithRunningVMs = new HashSet<>();
        List<String> hostNotRespondingList = new ArrayList<>();
        List<String> hostsWithNonMigratableVms = new ArrayList<>();
        List<String> hostsWithVmsWithPluggedDiskSnapshots = new ArrayList<>();
        List<String> nonMigratableVms = new ArrayList<>();

        for (Guid vdsId : getParameters().getVdsIdList()) {
            VDS vds = vdsDao.get(vdsId);
            if (vds == null) {
                log.error("ResourceManager::vdsMaintenance could not find VDS '{}'", vdsId);
                result = failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
                continue;
            }
            //TODO make a more efficient call but normally the command just loads one cluster anyway
            if (!clusters.containsKey(vds.getClusterId())){
                final Cluster cluster = clusterDao.get(vds.getClusterId());
                clusters.put(cluster.getId(), cluster);
            }
            if (!vdssToMaintenance.containsKey(vdsId)) {
                vdssToMaintenance.put(vdsId, vds);
                if (vds.getSpmStatus() == VdsSpmStatus.SPM) {
                    addSharedLockEntry(vds);
                }
            }
            if (getParameters().isStopGlusterService() && !vds.getClusterSupportsGlusterService()) {
                result = failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_SERVICE_MAINTENANCE_NOT_SUPPORTED_FOR_CLUSTER);
                break;
            }
        }
        result = result && acquireLockInternal();
        if (result) {
            // check if one of the target vdss is spm, if so check that there are no
            // tasks running
            for (Guid vdsId : getParameters().getVdsIdList()) {
                VDS vds = vdssToMaintenance.get(vdsId);
                if (vds != null) {
                    if ((vds.getStatus() != VDSStatus.Maintenance) && (vds.getStatus() != VDSStatus.NonResponsive)
                            && (vds.getStatus() != VDSStatus.Up) && (vds.getStatus() != VDSStatus.Error)
                            && (vds.getStatus() != VDSStatus.PreparingForMaintenance)
                            && (vds.getStatus() != VDSStatus.Down)
                            && (vds.getStatus() != VDSStatus.NonOperational
                            && (vds.getStatus() != VDSStatus.InstallFailed))) {
                        result = failValidation(EngineMessage.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_OPERATIONAL);
                    } else {
                        List<VM> vms = vmDao.getAllRunningForVds(vdsId);
                        if (!vms.isEmpty()) {
                            vdsWithRunningVMs.add(vdsId);
                        }
                        clustersAsSet.add(vds.getClusterId());

                        List<String> nonMigratableVmDescriptionsToFrontEnd = new ArrayList<>();
                        for (VM vm : vms) {
                            // The Hosted Engine VM is migrated by the HA agent;
                            // And they need safe place for migration
                            if (vm.isHostedEngine()) {
                                List<VDS> clusterVdses =
                                        vdsDao.getAllForClusterWithStatus(vds.getClusterId(), VDSStatus.Up);
                                if (!HostedEngineHelper.haveHostsAvailableForHE(
                                        clusterVdses,
                                        getParameters().getVdsIdList())) {
                                    failValidation(
                                            EngineMessage.VDS_CANNOT_MAINTENANCE_NO_ALTERNATE_HOST_FOR_HOSTED_ENGINE);
                                    return false;
                                }
                            }

                            boolean vmNonMigratable = vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST ||
                                    (vm.getMigrationSupport() == MigrationSupport.IMPLICITLY_NON_MIGRATABLE && getParameters().getIsInternal());

                            // The Hosted Engine VM is migrated by the HA agent;
                            // other non-migratable VMs are reported
                            if (vmNonMigratable && !vm.isHostedEngine()) {
                                nonMigratableVmDescriptionsToFrontEnd.add(vm.getName());
                            }
                        }

                        List<AsyncTask> asyncTasks = null;
                        if (nonMigratableVmDescriptionsToFrontEnd.size() > 0) {
                            hostsWithNonMigratableVms.add(vds.getName());
                            nonMigratableVms.addAll(nonMigratableVmDescriptionsToFrontEnd);

                            // The non migratable VM names will be comma separated
                            log.error("VDS '{}' contains non migratable VMs", vdsId);
                            result = false;
                        } else if (!validate(new MultipleVmsValidator(vms)
                                .vmNotHavingPluggedDiskSnapshots(EngineMessage.VDS_CANNOT_MAINTENANCE_VM_HAS_PLUGGED_DISK_SNAPSHOT))) {
                            hostsWithVmsWithPluggedDiskSnapshots.add(vds.getName());
                            result = false;
                        } else if (vds.getStatus() == VDSStatus.Maintenance) {
                            result = failValidation(EngineMessage.VDS_CANNOT_MAINTENANCE_VDS_IS_IN_MAINTENANCE);
                        } else if (vds.getSpmStatus() == VdsSpmStatus.Contending) {
                            result = failValidation(EngineMessage.VDS_CANNOT_MAINTENANCE_SPM_CONTENDING);
                        } else if (vds.getStatus() == VDSStatus.NonResponsive && vds.getVmCount() > 0) {
                            result = false;
                            hostNotRespondingList.add(vds.getName());
                        } else if (vds.getStatus() == VDSStatus.NonResponsive
                                && vds.getSpmStatus() != VdsSpmStatus.None) {
                            result = failValidation(EngineMessage.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_AND_IS_SPM);
                        } else if (vds.getSpmStatus() == VdsSpmStatus.SPM && vds.getStatus() == VDSStatus.Up &&
                                ((asyncTasks = asyncTaskDao.getAsyncTaskIdsByStoragePoolId(vds.getStoragePoolId()))).size() > 0) {
                            String runningTasks = asyncTasks
                                    .stream()
                                    .map(AsyncTask::toString)
                                    .collect(Collectors.joining("\n"));
                            log.warn("There are running tasks on the SPM: '{}'", runningTasks);
                            result = failValidation(EngineMessage.VDS_CANNOT_MAINTENANCE_SPM_WITH_RUNNING_TASKS);
                        } else if (!validateNoRunningJobs(vds)) {
                            result = false;
                        } else if (!validateNoActiveImageTransfers(vds)) {
                            result = false;
                        } else if (!clusters.get(vds.getClusterId()).isInUpgradeMode()) {
                            result = handlePositiveEnforcingAffinityGroup(vdsId, vms, clusters.get(vds.getClusterId()).getCompatibilityVersion());
                        }
                    }
                }
            }

            // If one of the host is non responsive with running VM's, add a Validate message.
            handleNonResponsiveHosts(hostNotRespondingList);

            // If one of the vms is non migratable, add a Validate message.
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
                List<String> problematicClusters = new ArrayList<>();
                List<String> allHostsWithRunningVms = new ArrayList<>();
                for (Guid clusterID : clustersAsSet) {
                    List<VDS> vdsList = vdsDao.getAllForCluster(clusterID);
                    boolean vdsForMigrationExists =
                            checkIfThereIsVDSToHoldMigratedVMs(getParameters().getVdsIdList(), vdsList);

                    if (!vdsForMigrationExists) {
                        List<String> candidateHostsWithRunningVms = new ArrayList<>();
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
                    addValidationMessage(EngineMessage.CANNOT_MAINTENANCE_VDS_RUN_VMS_NO_OTHER_RUNNING_VDS);
                    String commaDelimitedClusters = StringUtils.join(problematicClusters, ",");
                    getReturnValue().getValidationMessages().add(String.format("$ClustersList %1$s",
                            commaDelimitedClusters));
                    getReturnValue().getValidationMessages().add(String.format("$HostsList %1$s",
                            StringUtils.join(allHostsWithRunningVms, ",")));
                }
            }

            if (result && !getParameters().isForceMaintenance()) {
                result = validateGlusterParams(clustersAsSet);
            }
        }
        if (!result) {
            addMaintenanceFailedReason();
        }
        return result;
    }

    private void addMaintenanceFailedReason() {
        addCustomValue("Message",
                String.join(",",
                        backend.getErrorsTranslator().translateErrorText(getReturnValue().getValidationMessages())));
        auditLogDirector.log(this, AuditLogType.GENERIC_ERROR_MESSAGE);
    }

    private boolean validateNoRunningJobs(VDS vds) {
        List<Step> steps =
                stepDao.getStartedStepsByStepSubjectEntity(new SubjectEntity(VdcObjectType.EXECUTION_HOST,
                        vds.getId()));
        if (!steps.isEmpty()) {
            List<String> replacements = new ArrayList<>(2);
            replacements.add(ReplacementUtils.createSetVariableString("host", vds.getName()));
            replacements.addAll(ReplacementUtils.replaceWith("jobs",
                    steps.stream().map(s -> s.getDescription()).collect(Collectors.toList())));
            return failValidation(EngineMessage.VDS_CANNOT_MAINTENANCE_HOST_WITH_RUNNING_OPERATIONS, replacements);
        }

        return true;
    }

    private boolean validateNoActiveImageTransfers(VDS vds) {
        List<ImageTransfer> transfers = imageTransferDao.getByVdsId(vds.getId());
        if (!transfers.stream().allMatch(ImageTransfer::isPausedOrFinished)) {
            List<String> replacements = new ArrayList<>(3);
            replacements.add(ReplacementUtils.createSetVariableString("host", vds.getName()));
            replacements.addAll(ReplacementUtils.replaceWith("disks",
                    transfers.stream()
                            .filter(imageTransfer -> !imageTransfer.isPausedOrFinished())
                            .map(ImageTransfer::getDiskId)
                            .sorted()
                            .collect(Collectors.toList())));
            return failValidation(EngineMessage.VDS_CANNOT_MAINTENANCE_HOST_WITH_RUNNING_IMAGE_TRANSFERS, replacements);
        }
        return true;
    }

    /*
     * Validates gluster specific properties before moving the host to maintenance. Following things will be checked as
     * part of this check 1. Ensure gluster quorum can be met for all the volumes 2. Ensure there is no unsynced entry
     * present in the bricks
     */
    private boolean validateGlusterParams(Set<Guid> clustersAsSet) {
        boolean result = true;
        List<String> volumesWithoutQuorum = new ArrayList<>();
        if (!getParameters().isStopGlusterService()) {
            // if gluster services are not stopped as part of maintenance, the quorum checks are not needed
            return true;
        }
        for (Guid clusterId : clustersAsSet) {
            Cluster cluster = clusterDao.get(clusterId);
            if (cluster.supportsGlusterService()) {
                volumesWithoutQuorum.addAll(
                        glusterHostValidator.checkGlusterQuorum(cluster, getParameters().getVdsIdList()));
            }
        }
        if (!volumesWithoutQuorum.isEmpty()) {
            addValidationMessage(
                    EngineMessage.VDS_CANNOT_MAINTENANCE_GLUSTER_QUORUM_CANNOT_BE_MET);
            addValidationMessageVariable("VolumesList", StringUtils.join(volumesWithoutQuorum, ","));
            String hostList = vdssToMaintenance.values()
                    .stream()
                    .map(host -> host.getName())
                    .collect(Collectors.joining(","));
            addValidationMessageVariable("HostsList", hostList);
            result = false;
        }

        if (result) {
            Map<Guid, List<String>> unsyncedEntries =
                    glusterHostValidator.checkUnsyncedEntries(getParameters().getVdsIdList());
            if (!unsyncedEntries.isEmpty()) {
                addValidationMessage(
                        EngineMessage.VDS_CANNOT_MAINTENANCE_UNSYNCED_ENTRIES_PRESENT_IN_GLUSTER_BRICKS);
                addValidationMessageVariable("BricksList", StringUtils.join(unsyncedEntries.values(), ","));
                String hostsWithUnsyncedEntries = unsyncedEntries.keySet()
                        .stream()
                        .map(hostId -> vdssToMaintenance.get(hostId).getName())
                        .collect(Collectors.joining(","));
                addValidationMessageVariable("HostsList", hostsWithUnsyncedEntries);
                result = false;
            }
        }
        return result;
    }

    /**
     * For VM to host affinity, a VM with positive enforcing affinity cannot be migrated
     * if there is no other host in the affinity group.
     *
     * @param vdsId      current host id
     * @param runningVms current running vms on host
     * @return true if migration is possible, false - otherwise
     */
    private boolean handlePositiveEnforcingAffinityGroup(Guid vdsId, List<VM> runningVms, Version compatibilityVersion) {
        if (Config.getValue(ConfigValues.IgnoreVmToVmAffinityForHostMaintenance, compatibilityVersion.getValue())) {
            return true;
        }

        List<AffinityGroup> affinityGroups =
                affinityGroupDao.getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId(vdsId);
        if (!affinityGroups.isEmpty()) {

            List<Object> items = new ArrayList<>();
            affinityGroups.stream()
                    .filter(ag -> ag.getVdsIds().size() == 1 && Objects.equals(ag.getVdsIds().get(0), vdsId))
                    .forEach(affinityGroup -> {
                        items.add(String.format("%1$s (%2$s)",
                                affinityGroup.getName(),
                                StringUtils.join(affinityGroup.getVmEntityNames(), " ,")));
                    });

            if (!items.isEmpty()) {
                addValidationMessage(EngineMessage.VDS_CANNOT_MAINTENANCE_VDS_HAS_AFFINITY_VMS);
                getReturnValue().getValidationMessages()
                        .addAll(ReplacementUtils.replaceWith("AFFINITY_GROUPS_VMS", items));
                return false;
            }
        }

        return true;
    }

    private void addSharedLockEntry(VDS vds) {
        if (sharedLockMap == null) {
            sharedLockMap = new HashMap<>();
        }
        sharedLockMap.put(vds.getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    /**
     * If found hosts which has non-migratable VM's on them, add the host names and Vm's to the message.
     *
     * @param hostNotRespondingList
     *            - List of non responsive hosts with running VM's on them.
     */
    private void handleNonMigratableVms(List<String> hostsWithNonMigratableVms, List<String> nonMigratableVms) {
        if (!nonMigratableVms.isEmpty()) {
            addValidationMessage(EngineMessage.VDS_CANNOT_MAINTENANCE_IT_INCLUDES_NON_MIGRATABLE_VM);
            getReturnValue().getValidationMessages().add(String.format("$VmsList %1$s",
                    StringUtils.join(nonMigratableVms, " , ")));
            getReturnValue().getValidationMessages().add(String.format("$HostsList %1$s",
                    StringUtils.join(hostsWithNonMigratableVms, " , ")));
        }
    }

    private boolean checkIfThereIsVDSToHoldMigratedVMs(Iterable<Guid> vdsIDListFromParams, List<VDS> vdsInCluster) {
        // Checks if the number of UP VDS in the parameters for a given cluster
        // is
        // less than the number of UP parameters in that cluster - if this is
        // the case, we have
        // a VDS to migrate to
        Set<Guid> upVdsIDsInCluster = new HashSet<>();
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
            addValidationMessage(EngineMessage.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_RESPONDING_WITH_VMS);
            getReturnValue().getValidationMessages().add(String.format("$HostNotResponding %1$s",
                    StringUtils.join(hostNotRespondingList, ",")));
        }
    }

    private void handleHostsWithVmsWithPluggedDiskSnapshots(List<String> hostsWithVmsWithPluggedDiskSnapshots) {
        if (!hostsWithVmsWithPluggedDiskSnapshots.isEmpty()) {
            getReturnValue().getValidationMessages().add(String.format("$HostsList %1$s",
                    StringUtils.join(hostsWithVmsWithPluggedDiskSnapshots, ",")));
        }
    }

    private void addClusterDetails(Guid clusterID, List<String> clustersWithRunningVms) {
        if (clusterID != null && !clusterID.equals(Guid.Empty)) {
            Cluster cluster = clusterDao.getWithRunningVms(clusterID);
            if (cluster != null) {
                clustersWithRunningVms.add(cluster.getName());
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

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
