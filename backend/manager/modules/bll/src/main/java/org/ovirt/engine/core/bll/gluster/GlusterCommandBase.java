package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Base class for all Gluster commands
 */
public abstract class GlusterCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {
    private static final long serialVersionUID = -7394070330293300587L;

    public GlusterCommandBase(T params) {
        super(params);
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
     * Returns a server that is in {@link VDSStatus#Up} status.<br>
     * This server is chosen randomly from all the Up servers.
     *
     * @return One of the servers in up status
     */
    protected VDS getUpServer() {
        List<VDS> servers = DbFacade.getInstance()
                .getVdsDAO()
                .getAllForVdsGroupWithStatus(getVdsGroupId(), VDSStatus.Up);
        if (servers == null || servers.isEmpty()) {
            throw new VdcBLLException(VdcBllErrors.NO_UP_SERVER_FOUND);
        }
        return servers.get(GlusterCoreUtil.random(servers.size() - 1));
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
}
