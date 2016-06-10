package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class RemoveVdsCommand<T extends RemoveVdsParameters> extends VdsCommand<T> {

    private AuditLogType errorType = AuditLogType.USER_FAILED_REMOVE_VDS;
    private VDS upServer;

    public RemoveVdsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void executeCommand() {
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
            removeVdsStatisticsFromDb();
            removeVdsDynamicFromDb();
            removeVdsStaticFromDb();
            return null;
        });
        removeVdsFromCollection();
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        boolean returnValue = canRemoveVds(getVdsId(), getReturnValue().getValidationMessages());
        StoragePool storagePool = getStoragePoolDao().getForVds(getParameters().getVdsId());

        if (returnValue && storagePool != null && storagePool.isLocal()) {
            if (!getStorageDomainDao().getAllForStoragePool(storagePool.getId()).isEmpty()) {
                returnValue = failValidation(EngineMessage.VDS_CANNOT_REMOVE_HOST_WITH_LOCAL_STORAGE);
            }
        }

        // Perform volume bricks on server and up server null check
        if (returnValue && isGlusterEnabled()) {
            upServer = getGlusterUtils().getUpServer(getClusterId());
            if (!getParameters().isForceAction()) {
                // fail if host has bricks on a volume
                if (hasVolumeBricksOnServer()) {
                    returnValue = failValidation(EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                } else if (upServer == null && clusterHasMultipleHosts()) {
                    // fail if there is no up server in cluster, and if host being removed is not
                    // the last server in cluster
                    addValidationMessageVariable("clusterName", getCluster().getName());
                    returnValue = failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
                }
            } else {
                // if force, cannot remove only if there are bricks on server and there is an up server.
                if (hasVolumeBricksOnServer() && upServer != null) {
                    returnValue = failValidation(EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                }
            }
        }

        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
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

    private void removeVdsStaticFromDb() {
        getVdsStaticDao().remove(getVdsId());
    }

    private void removeVdsDynamicFromDb() {
        getVdsDynamicDao().remove(getVdsId());
    }

    private void removeVdsStatisticsFromDb() {
        getVdsStatisticsDao().remove(getVdsId());
    }

    protected VdsStatisticsDao getVdsStatisticsDao() {
        return getDbFacade().getVdsStatisticsDao();
    }

    private boolean canRemoveVds(Guid vdsId, List<String> text) {
        boolean returnValue = true;
        // check if vds id is valid
        VDS vds = getVdsDao().get(vdsId);
        if (vds == null) {
            text.add(EngineMessage.VDS_INVALID_SERVER_ID.toString());
            returnValue = false;
        } else if (!statusLegalForRemove(vds)) {
            text.add(EngineMessage.VDS_CANNOT_REMOVE_VDS_STATUS_ILLEGAL.toString());
            returnValue = false;
        } else if (vds.getVmCount() > 0) {
            text.add(EngineMessage.VDS_CANNOT_REMOVE_VDS_DETECTED_RUNNING_VM.toString());
            returnValue = false;
        } else {
            List<String> vmNamesPinnedToHost = getVmStaticDao().getAllNamesPinnedToHost(vdsId);
            if (!vmNamesPinnedToHost.isEmpty()) {
                text.add(EngineMessage.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS.toString());
                text.add(String.format("$VmNames %s", StringUtils.join(vmNamesPinnedToHost, ',')));
                returnValue = false;
            }
        }
        return returnValue;
    }

    private boolean isGlusterEnabled() {
        return getCluster().supportsGlusterService();
    }

    private boolean hasVolumeBricksOnServer() {
        if (getGlusterBrickDao().getGlusterVolumeBricksByServerId(getVdsId()).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void removeGlusterVolumesFromDb() {
        getGlusterVolumeDao().removeByClusterId(getClusterId());
    }

    private void removeGlusterHooksFromDb() {
        getGlusterHooksDao().removeAllInCluster(getClusterId());
    }

    public GlusterUtil getGlusterUtils() {
        return GlusterUtil.getInstance();
    }

    public ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    private void glusterHostRemove() {
        if (clusterHasMultipleHosts() && !hasVolumeBricksOnServer()) {
            try (EngineLock lock = getGlusterUtils().acquireGlusterLockWait(getClusterId())) {
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
                            if (!getGlusterUtils().isHostExists(glusterServers, getVds())) {
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
                if (getClusterUtils().getServerCount(getClusterId()) == 2) {
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
        }
        else {
            return null;
        }
    }

    private boolean clusterHasMultipleHosts() {
        return getClusterUtils().hasMultipleServers(getClusterId());
    }

    private void removeOtherKnowAddressesForGlusterServer(Guid lastServerId) {
        getDbFacade().getGlusterServerDao().updateKnownAddresses(lastServerId, null);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }
}
