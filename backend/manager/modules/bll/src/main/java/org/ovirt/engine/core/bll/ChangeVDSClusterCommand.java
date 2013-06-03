package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.AddGlusterServerVDSParameters;
import org.ovirt.engine.core.common.vdscommands.gluster.RemoveGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ChangeVDSClusterCommand<T extends ChangeVDSClusterParameters> extends VdsCommand<T> {

    private StoragePool targetStoragePool;

    private VDSGroup targetCluster;

    private AuditLogType errorType = AuditLogType.USER_FAILED_UPDATE_VDS;

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId
     */
    public ChangeVDSClusterCommand(Guid commandId) {
        super(commandId);
    }

    public ChangeVDSClusterCommand(T params) {
        super(params);
    }

    @Override
    protected boolean canDoAction() {
        VDS vds = getVds();
        if (vds == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            return false;
        }
        if (!ObjectIdentityChecker.CanUpdateField(vds, "vdsGroupId", vds.getStatus())) {
            addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE);
            return false;
        }

        if (getTargetCluster() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }

        targetStoragePool = DbFacade.getInstance().getStoragePoolDao().getForVdsGroup(getTargetCluster().getId());
        if (targetStoragePool != null && targetStoragePool.getstorage_pool_type() == StorageType.LOCALFS) {
            if (!DbFacade.getInstance().getVdsStaticDao().getAllForVdsGroup(getParameters().getClusterId()).isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE);
                return false;
            }
        }

        if (getVdsGroup().supportsGlusterService()) {
            if (getGlusterUtils().hasBricks(getVdsId())) {
                addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
                return false;
            }

            if (!hasUpServer(getSourceCluster())) {
                return false;
            }

        }

        if (getTargetCluster().supportsGlusterService() && !hasUpServerInTarget(getTargetCluster())) {
            return false;
        }
        return true;
    }

    private boolean hasUpServer(VDSGroup cluster) {
        if (getClusterUtils().hasMultipleServers(cluster.getId())
                && getClusterUtils().getUpServer(cluster.getId()) == null) {
            addNoUpServerMessage(cluster);
            return false;
        }
        return true;
    }

    private void addNoUpServerMessage(VDSGroup cluster) {
        addCanDoActionMessage(String.format("$clusterName %1$s", cluster.getname()));
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
    }

    private boolean hasUpServerInTarget(VDSGroup cluster) {
        if (getClusterUtils().hasServers(cluster.getId())
                && getClusterUtils().getUpServer(cluster.getId()) == null) {
            addNoUpServerMessage(cluster);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {

        final Guid targetClusterId = getParameters().getClusterId();
        if (getSourceCluster().getId().equals(targetClusterId)) {
            setSucceeded(true);
            return;
        }

        // save the new cluster id
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                VdsStatic staticData = getVds().getStaticData();
                getCompensationContext().snapshotEntity(staticData);
                staticData.setVdsGroupId(targetClusterId);
                DbFacade.getInstance().getVdsStaticDao().update(staticData);
                getCompensationContext().stateChanged();
                return null;
            }
        });

        getParameters().setCompensationEnabled(true);
        getParameters().setTransactionScopeOption(TransactionScopeOption.RequiresNew);

        if (targetStoragePool != null
                && (getSourceCluster().getStoragePoolId()== null || !targetStoragePool.getId().equals(getSourceCluster().getStoragePoolId()))) {
            VdcReturnValueBase addVdsSpmIdReturn =
                    Backend.getInstance().runInternalAction(VdcActionType.AddVdsSpmId,
                            getParameters(),
                            new CommandContext(getCompensationContext()));
            if (!addVdsSpmIdReturn.getSucceeded()) {
                setSucceeded(false);
                getReturnValue().setFault(addVdsSpmIdReturn.getFault());
                return;
            }
        }


        if (getSourceCluster().supportsGlusterService() && getClusterUtils().hasServers(getSourceCluster().getId())) {
            if (!glusterHostRemove(getSourceCluster().getId())) {
                return;
            }
        }

        if (getTargetCluster().supportsGlusterService()
                && getClusterUtils().hasMultipleServers(getTargetCluster().getId())) {
            if (!glusterHostAdd(getTargetCluster().getId())) {
                return;
            }
        }

        if (getSourceCluster().getStoragePoolId() != null
                && (targetStoragePool== null || !getSourceCluster().getStoragePoolId().equals(targetStoragePool.getId()))) {
            getVdsSpmIdMapDAO().removeByVdsAndStoragePool(getVds().getId(), getSourceCluster().getStoragePoolId());
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VDS : errorType;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>(2);
        permissionList.add(new PermissionSubject(getParameters().getVdsId(), VdcObjectType.VDS, getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getParameters().getClusterId(), VdcObjectType.VdsGroups, getActionType().getActionGroup()));
        List<PermissionSubject> unmodifiableList = Collections.unmodifiableList(permissionList);
        return unmodifiableList;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
    }

    private boolean glusterHostRemove(Guid sourceClusterId) {
        String hostName =
                (getVds().getHostName().isEmpty()) ? getVds().getManagementIp()
                        : getVds().getHostName();
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.RemoveGlusterServer,
                        new RemoveGlusterServerVDSParameters((getClusterUtils().getUpServer(sourceClusterId)).getId(),
                                hostName,
                                false));

        if (!returnValue.getSucceeded()) {
            handleVdsError(returnValue);
            errorType = AuditLogType.GLUSTER_SERVER_REMOVE_FAILED;
            return false;
        }
        return true;
    }

    private boolean glusterHostAdd(Guid targetClusterId) {
        String hostName =
                (getVds().getHostName().isEmpty()) ? getVds().getManagementIp()
                        : getVds().getHostName();
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.AddGlusterServer,
                        new AddGlusterServerVDSParameters(getClusterUtils().getUpServer(targetClusterId).getId(),
                                hostName));
        if (!returnValue.getSucceeded()) {
            handleVdsError(returnValue);
            errorType = AuditLogType.GLUSTER_SERVER_ADD_FAILED;
            return false;
        }
        return true;
    }

    private ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    private GlusterDBUtils getGlusterUtils() {
        return GlusterDBUtils.getInstance();
    }

    private VDSGroup getSourceCluster() {
        return getVdsGroup();
    }

    private VDSGroup getTargetCluster() {
        if (targetCluster == null) {
            targetCluster = DbFacade.getInstance().getVdsGroupDao().get(getParameters().getClusterId());
        }
        return targetCluster;
    }

}
