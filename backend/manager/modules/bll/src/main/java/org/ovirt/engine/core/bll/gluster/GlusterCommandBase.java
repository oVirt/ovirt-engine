package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.VdsStaticDAO;

/**
 * Base class for all Gluster commands
 */
public abstract class GlusterCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {
    private static final long serialVersionUID = -7394070330293300587L;
    protected AuditLogType errorType;
    protected VDS upServer;

    public GlusterCommandBase(T params) {
        super(params);
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getVdsGroupId().toString(), LockingGroup.GLUSTER.name());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.bll.CommandBase#getPermissionCheckSubjects()
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // By default, check permissions at cluster level. Commands that need
        // more granular permissions can override this method.
        return Collections.singletonList(new PermissionSubject(getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
    }

    /**
     * This server is chosen randomly from all the Up servers.
     *
     * @return One of the servers in up status
     */
    protected VDS getUpServer() {
        return getClusterUtils().getUpServer(getVdsGroupId());
    }

    private ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        upServer = getUpServer();
        if (upServer == null) {
            addCanDoActionMessage(String.format("$clusterName %1$s", getVdsGroup().getname()));
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_UP_SERVER_FOUND);
            return false;
        }
        return true;
    }

    /**
     * Executes given BLL action, updates the success flag based on the result, and returns the result
     *
     * @param actionType
     * @param params
     * @return
     */
    protected VdcReturnValueBase runBllAction(VdcActionType actionType, VdcActionParametersBase params) {
        VdcReturnValueBase returnValue = Backend.getInstance().runInternalAction(actionType, params);
        setSucceeded(returnValue.getSucceeded());
        return returnValue;
    }

    protected void handleVdsErrors(AuditLogType errType, List<String> errors) {
        errorType = errType;
        getReturnValue().getExecuteFailedMessages().addAll(errors);
        getReturnValue().getFault().setMessage(StringUtils.join(errors, SystemUtils.LINE_SEPARATOR));
    }

    protected void handleVdsError(AuditLogType errType, String error) {
        errorType = errType;
        getReturnValue().getExecuteFailedMessages().add(error);
        getReturnValue().getFault().setMessage(error);
    }

    protected boolean updateBrickServerNames(List<GlusterBrickEntity> bricks, boolean addCanDoActionMessage) {
        for (GlusterBrickEntity brick : bricks) {
            if (!updateBrickServerName(brick, addCanDoActionMessage)) {
                return false;
            }
        }
        return true;
    }

    protected boolean updateBrickServerName(GlusterBrickEntity brick, boolean addCanDoActionMessage) {
        VdsStatic server = getVdsStaticDao().get(brick.getServerId());
        if ((server == null || !server.getvds_group_id().equals(getVdsGroupId()))) {
            if (addCanDoActionMessage) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_BRICK_SERVER_ID);
            }
            return false;
        }
        brick.setServerName(server.gethost_name());
        return true;
    }

    public VdsStaticDAO getVdsStaticDao() {
        return getDbFacade().getVdsStaticDao();
    }
}
