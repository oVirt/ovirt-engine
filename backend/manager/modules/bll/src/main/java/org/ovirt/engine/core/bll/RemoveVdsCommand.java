package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveVdsCommand<T extends RemoveVdsParameters> extends VdsCommand<T> {

    private AuditLogType errorType = AuditLogType.USER_FAILED_REMOVE_VDS;
    private VDS upServer;

    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;
    @Inject
    private VdsStatisticsDao vdsStatisticsDao;
    @Inject
    private TagDao tagDao;
    @Inject
    private GlusterServerDao glusterServerDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private GlusterBrickDao glusterBrickDao;
    @Inject
    private GlusterVolumeDao glusterVolumeDao;
    @Inject
    private GlusterHooksDao glusterHooksDao;
    @Inject
    private ClusterUtils clusterUtils;
    @Inject
    private HostLocking hostLocking;
    @Inject
    private AnsibleExecutor ansibleExecutor;

    public RemoveVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected LockProperties getLockProperties() {
        return LockProperties.create(Scope.Command).withWaitTimeout(TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    protected void executeCommand() {
        if (isForceRemovalOfUnmanagedHost(getVds())) {
            removeHostFromDB(getVdsId());
            removeVdsFromCollection();
            setSucceeded(true);
            return;
        }

        /**
         * If upserver is null and force action is true, then don't try for gluster host remove, simply remove the host
         * entry from database.
         */
        if (isGlusterEnabled() && upServer != null) {
            glusterHostRemove();
            if (!getSucceeded()) {
                return;
            }
        }

        /**
         * If the removing server is the last server in the cluster , then clear the gluster
         * volumes and hooks from the database
         * if not force, host remove would have failed if there were volumes, so safe to
         * clean up volumes in DB.
         */
        if (!clusterHasMultipleHosts()) {
            removeGlusterVolumesFromDb();
            removeGlusterHooksFromDb();
        }

        TransactionSupport.executeInNewTransaction(() -> {
            removeHostFromDB(getVdsId());
            return null;
        });

        removeVdsFromCollection();
        runAnsibleRemovePlaybook();
        setSucceeded(true);
    }

    private void removeHostFromDB(Guid hostId) {
        vdsStatisticsDao.remove(hostId);
        tagDao.detachVdsFromAllTags(hostId);
        vdsDynamicDao.remove(hostId);
        vdsStaticDao.remove(hostId);
    }

    private boolean isForceRemovalOfUnmanagedHost(VDS vds) {
        return !vds.isManaged() && isInternalExecution();
    }

    @SuppressWarnings("unchecked")
    private void runAnsibleRemovePlaybook() {
        VDS vds = getVds();
        AuditLogable auditable = new AuditLogableImpl();
        auditable.setVdsName(vds.getName());
        auditable.setVdsId(vds.getId());
        auditable.setCorrelationId(getCorrelationId());

        try {
            AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                    .hosts(vds)
                    .playbook(AnsibleConstants.HOST_REMOVE_PLAYBOOK);

            auditLogDirector.log(auditable, AuditLogType.VDS_ANSIBLE_HOST_REMOVE_STARTED);

            AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(commandConfig);

            auditable.addCustomValue("LogFile", ansibleReturnValue.getLogFile().toString());

            AnsibleReturnCode ansibleReturnCode = ansibleReturnValue.getAnsibleReturnCode();
            if (ansibleReturnCode == AnsibleReturnCode.OK
                    // we did best to remove it and if it failed ... so be it bugzilla: 1707451
                    || isUnreachableHostDown(vds.getStatus(), ansibleReturnCode)) {
                auditLogDirector.log(auditable, AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FINISHED);
            } else {
                auditLogDirector.log(auditable, AuditLogType.VDS_ANSIBLE_HOST_REMOVE_FAILED);
            }

            if (ansibleReturnCode == AnsibleReturnCode.UNREACHABLE) {
                auditLogDirector.log(auditable, AuditLogType.VDS_ANSIBLE_HOST_REMOVE_NO_SUCCESS);
            }
        } catch (Exception e) {
            auditable.addCustomValue("Message", e.getMessage());
            auditLogDirector.log(auditable, AuditLogType.VDS_ANSIBLE_HOST_REMOVE_EXECUTION_FAILED);
        }
    }

    private boolean isUnreachableHostDown(VDSStatus vdsStatus, AnsibleReturnCode returnCode) {
        return (returnCode == AnsibleReturnCode.UNREACHABLE
                || returnCode == AnsibleReturnCode.PARSE_ERROR) // due: https://github.com/ansible/ansible/issues/19720
                && vdsStatus == VDSStatus.Down;
    }

    @Override
    protected boolean validate() {
        VDS vds = getVds();
        if (vds == null) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
        }

        if (isForceRemovalOfUnmanagedHost(vds)) {
            return true;
        }

        if (!statusLegalForRemove(vds)) {
            return failValidation(EngineMessage.VDS_CANNOT_REMOVE_VDS_STATUS_ILLEGAL);
        }

        if (vds.getVmCount() > 0) {
            return failValidation(EngineMessage.VDS_CANNOT_REMOVE_VDS_DETECTED_RUNNING_VM);
        }

        List<VM> vms = vmDao.getAllPinnedToHost(vds.getId());
        if (!vms.isEmpty()) {
            List<String> vmNamesPinnedToHost = vms.stream()
                    .filter(vm -> vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST)
                    .map(VM::getName)
                    .collect(Collectors.toList());
            if (!vmNamesPinnedToHost.isEmpty()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS,
                        String.format("$VmNames %s", StringUtils.join(vmNamesPinnedToHost, ',')));
            }

            List<String> vmNamesWithHostDevices = vms.stream()
                    .filter(this::isVmAssignedWithHostDevices)
                    .map(VM::getName)
                    .collect(Collectors.toList());
            if (!vmNamesWithHostDevices.isEmpty()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DETECTED_ASSIGNED_HOST_DEVICES,
                        String.format("$VmNames %s", StringUtils.join(vmNamesWithHostDevices, ',')));
            }
        }

        StoragePool storagePool = storagePoolDao.getForVds(getParameters().getVdsId());
        if (storagePool != null && storagePool.isLocal()
                && !storageDomainDao.getAllForStoragePool(storagePool.getId()).isEmpty()) {
            return failValidation(EngineMessage.VDS_CANNOT_REMOVE_HOST_WITH_LOCAL_STORAGE);
        }

        // Perform volume bricks on server and up server null check
        if (isGlusterEnabled()) {
            upServer = glusterUtil.getUpServer(getClusterId());
            if (!getParameters().isForceAction()) {
                // fail if host has bricks on a volume
                if (hasVolumeBricksOnServer()) {
                    return failValidation(EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                }
                if (upServer == null && clusterHasMultipleHosts()) {
                    // fail if there is no up server in cluster, and if host being removed is not
                    // the last server in cluster
                    addValidationMessageVariable("clusterName", getCluster().getName());
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
                }
            } else {
                // if force, cannot remove only if there are bricks on server and there is an up server.
                if (hasVolumeBricksOnServer() && upServer != null) {
                    return failValidation(EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                }
            }
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

    private boolean isVmAssignedWithHostDevices(VM vm) {
        return !vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.HOSTDEV).isEmpty();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VDS : errorType;
    }

    private boolean statusLegalForRemove(VDS vds) {
        return (vds.getStatus() == VDSStatus.NonResponsive) || (vds.getStatus() == VDSStatus.Maintenance)
                || (vds.getStatus() == VDSStatus.Down) || (vds.getStatus() == VDSStatus.Unassigned)
                || (vds.getStatus() == VDSStatus.InstallFailed) || (vds.getStatus() == VDSStatus.PendingApproval) || (vds
                    .getStatus() == VDSStatus.NonOperational) || (vds.getStatus() == VDSStatus.InstallingOS);
    }

    private void removeVdsFromCollection() {
        runVdsCommand(VDSCommandType.RemoveVds, new RemoveVdsVDSCommandParameters(getVdsId()));
    }

    private boolean isGlusterEnabled() {
        return getCluster().supportsGlusterService();
    }

    private boolean hasVolumeBricksOnServer() {
        if (glusterBrickDao.getGlusterVolumeBricksByServerId(getVdsId()).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void removeGlusterVolumesFromDb() {
        glusterVolumeDao.removeByClusterId(getClusterId());
    }

    private void removeGlusterHooksFromDb() {
        glusterHooksDao.removeAllInCluster(getClusterId());
    }

    private void glusterHostRemove() {
        if (clusterHasMultipleHosts() && !hasVolumeBricksOnServer()) {
            try (EngineLock lock = glusterUtil.acquireGlusterLockWait(getClusterId())) {
                VDSReturnValue returnValue =
                        runVdsCommand(
                                VDSCommandType.RemoveGlusterServer,
                                new RemoveGlusterServerVDSParameters(upServer.getId(),
                                        getVds().getHostName(),
                                        getParameters().isForceAction()));
                // If the host is already removed Cluster using Gluster CLI then we can setSucceeded to true.
                setSucceeded(returnValue.getSucceeded()
                        || EngineError.GlusterHostIsNotPartOfCluster == returnValue.getVdsError().getCode());
                if (!getSucceeded()) {
                    // VDSM in 3.3 (or less) cluster will return GlusterHostRemoveFailedException
                    // if the host is not part of the cluster
                    // So if peer detach is failed, check the peer list to decide that the host is not part of the
                    // cluster

                    if (returnValue.getVdsError().getCode() == EngineError.GlusterHostRemoveFailedException) {
                        List<GlusterServerInfo> glusterServers = getGlusterPeers(upServer);
                        if (glusterServers != null) {
                            if (!glusterUtil.isHostExists(glusterServers, getVds())) {
                                setSucceeded(true);
                            }
                        }
                    }
                    if (!getSucceeded()) {
                        getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
                        getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
                        errorType = AuditLogType.GLUSTER_SERVER_REMOVE_FAILED;
                        return;
                    }
                }
                // if last but one host in cluster, update the last host's known addresses
                if (clusterUtils.getServerCount(getClusterId()) == 2) {
                    removeOtherKnowAddressesForGlusterServer(upServer.getId());
                }
            }
        }
    }

    private List<GlusterServerInfo> getGlusterPeers(VDS upServer) {
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GlusterServersList,
                new VdsIdVDSCommandParametersBase(upServer.getId()));
        if (returnValue.getSucceeded()) {
            return (List<GlusterServerInfo>) returnValue.getReturnValue();
        } else {
            return null;
        }
    }

    private boolean clusterHasMultipleHosts() {
        return clusterUtils.hasMultipleServers(getClusterId());
    }

    private void removeOtherKnowAddressesForGlusterServer(Guid lastServerId) {
        glusterServerDao.updateKnownAddresses(lastServerId, null);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        locks.putAll(hostLocking.getVdsPoolAndStorageConnectionsLock(getParameters().getVdsId()));
        return locks;
    }
}
