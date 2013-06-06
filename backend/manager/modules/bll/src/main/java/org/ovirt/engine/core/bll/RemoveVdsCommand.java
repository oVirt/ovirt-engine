package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
@LockIdNameAttribute
public class RemoveVdsCommand<T extends RemoveVdsParameters> extends VdsCommand<T> {

    private AuditLogType errorType = AuditLogType.USER_FAILED_REMOVE_VDS;
    private VDS upServer;

    public RemoveVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        /**
         * If upserver is null and force action is true, then don't try for gluster host remove, simply remove the host
         * entry from database.
         */
        if (isGlusterEnabled() && upServer != null) {
            glusterHostRemove();
        }

        /**
         * If the removing server is the last server in the cluster and the force action is true, then clear the gluster
         * volumes from the database
         */
        if (!clusterHasMultipleHosts() && getParameters().isForceAction()) {
            removeGlusterVolumesFromDb();
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                RemoveVdsStatisticsFromDb();
                RemoveVdsDynamicFromDb();
                RemoveVdsStaticFromDb();
                return null;
            }
        });
        RemoveVdsFromCollection();
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = canRemoveVds(getVdsId(), getReturnValue().getCanDoActionMessages());
        StoragePool storagePool = getStoragePoolDAO().getForVds(getParameters().getVdsId());

        if (returnValue && storagePool != null && storagePool.getstorage_pool_type() == StorageType.LOCALFS) {
            if (!getStorageDomainDAO().getAllForStoragePool(storagePool.getId()).isEmpty()) {
                returnValue = failCanDoAction(VdcBllMessages.VDS_CANNOT_REMOVE_HOST_WITH_LOCAL_STORAGE);
            }
        }

        // Perform volume bricks on server and up server null check
        if (returnValue && isGlusterEnabled()) {
            upServer = getClusterUtils().getUpServer(getVdsGroupId());
            if (!getParameters().isForceAction()) {
                //  fail if host has bricks on a volume
                if (hasVolumeBricksOnServer()) {
                    returnValue = failCanDoAction(VdcBllMessages.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                } else if (upServer == null && clusterHasMultipleHosts()) {
                    //  fail if there is no up server in cluster, and if host being removed is not
                    //  the last server in cluster
                    addCanDoActionMessage(String.format("$clusterName %1$s", getVdsGroup().getname()));
                    returnValue = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
                 }
            } else {
                // if force, cannot remove only if there are bricks on server and there is an up server.
                if (hasVolumeBricksOnServer() && upServer != null ) {
                    returnValue = failCanDoAction(VdcBllMessages.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                }
            }
        }

        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VDS : errorType;
    }

    protected VdsDynamicDAO getVdsDynamicDAO() {
        return DbFacade.getInstance().getVdsDynamicDao();
    }

    private boolean statusLegalForRemove(VDS vds) {
        return ((vds.getStatus() == VDSStatus.NonResponsive) || (vds.getStatus() == VDSStatus.Maintenance)
                || (vds.getStatus() == VDSStatus.Down) || (vds.getStatus() == VDSStatus.Unassigned)
                || (vds.getStatus() == VDSStatus.InstallFailed) || (vds.getStatus() == VDSStatus.PendingApproval) || (vds
                    .getStatus() == VDSStatus.NonOperational));
    }

    private void RemoveVdsFromCollection() {
        // ResourceManager.Instance.removeVds(VdsId);
        Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.RemoveVds, new RemoveVdsVDSCommandParameters(getVdsId()));
    }

    private void RemoveVdsStaticFromDb() {
        DbFacade.getInstance().getVdsStaticDao().remove(getVdsId());
    }

    private void RemoveVdsDynamicFromDb() {
        getVdsDynamicDAO().remove(getVdsId());
    }

    private void RemoveVdsStatisticsFromDb() {
        DbFacade.getInstance().getVdsStatisticsDao().remove(getVdsId());
    }

    private boolean canRemoveVds(Guid vdsId, List<String> text) {
        boolean returnValue = true;
        // check if vds id is valid
        VDS vds = getVdsDAO().get(vdsId);
        if (vds == null) {
            text.add(VdcBllMessages.VDS_INVALID_SERVER_ID.toString());
            returnValue = false;
        } else if (!statusLegalForRemove(vds)) {
            text.add(VdcBllMessages.VDS_CANNOT_REMOVE_VDS_STATUS_ILLEGAL.toString());
            returnValue = false;
        } else if (vds.getVmCount() > 0) {
            text.add(VdcBllMessages.VDS_CANNOT_REMOVE_VDS_DETECTED_RUNNING_VM.toString());
            returnValue = false;
        } else {
            List<String> vmNamesPinnedToHost = getVmStaticDAO().getAllNamesPinnedToHost(vdsId);
            if (!vmNamesPinnedToHost.isEmpty()) {
                text.add(VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS.toString());
                text.add(String.format("$VmNames %s", StringUtils.join(vmNamesPinnedToHost, ',')));
                returnValue = false;
            }
        }
        return returnValue;
    }

    private boolean isGlusterEnabled() {
        return (getVdsGroup().supportsGlusterService());
    }

    protected GlusterBrickDao getGlusterBrickDao() {
        return getDbFacade().getGlusterBrickDao();
    }

    private boolean hasVolumeBricksOnServer() {
        if (getGlusterBrickDao().getGlusterVolumeBricksByServerId(getVdsId()).size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void removeGlusterVolumesFromDb() {
        getGlusterVolumeDao().removeByClusterId(getVdsGroupId());
    }

    public ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    private void glusterHostRemove() {
        if (clusterHasMultipleHosts() && !hasVolumeBricksOnServer()) {
            VDSReturnValue returnValue =
                    runVdsCommand(
                            VDSCommandType.RemoveGlusterServer,
                            new RemoveGlusterServerVDSParameters(upServer.getId(),
                                    getVds().getHostName(),
                                    getParameters().isForceAction()));
            setSucceeded(returnValue.getSucceeded());
            if (!getSucceeded()) {
                getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
                getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
                errorType = AuditLogType.GLUSTER_SERVER_REMOVE_FAILED;
                return;
            }
        }
    }

    private boolean clusterHasMultipleHosts() {
        return getClusterUtils().hasMultipleServers(getVdsGroupId());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<String, Pair<String, String>>();

        VDSGroup cluster = getVdsGroup();
        if (cluster == null || cluster.supportsVirtService()) {
            locks.put(getParameters().getVdsId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        // Need to acquire lock on cluster if the host belongs to a gluster cluster
        if (cluster != null && cluster.supportsGlusterService()) {
            locks.put(cluster.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }

        return locks;
    }
}
