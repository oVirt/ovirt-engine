package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.compat.Guid;

/**
 * Base class for all Gluster Hook commands
 */
public abstract class GlusterHookCommandBase<T extends VdcActionParametersBase> extends GlusterCommandBase<T> {

    public GlusterHookCommandBase(T params) {
        super(params);
    }

    @Override
    protected BackendInternal getBackend() {
        return super.getBackend();
    }

    protected List<VDS> getAllUpServers(Guid clusterId) {
        return getClusterUtils().getAllUpServers(clusterId);
    }

    protected void updateServerHookStatusInDb(Guid hookId, Guid serverId, GlusterHookStatus status) {
        getGlusterHooksDao().updateGlusterServerHookStatus(hookId, serverId, status);
    }

    protected void updateHookInDb(GlusterHookEntity hook) {
        getGlusterHooksDao().updateGlusterHook(hook);
    }

    protected void addServerHookInDb(GlusterServerHook serverHook) {
        getGlusterHooksDao().saveGlusterServerHook(serverHook);
    }

}
