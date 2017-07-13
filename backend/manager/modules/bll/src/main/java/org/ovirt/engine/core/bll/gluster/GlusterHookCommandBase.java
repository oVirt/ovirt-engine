package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;

/**
 * Base class for all Gluster Hook commands
 */
public abstract class GlusterHookCommandBase<T extends GlusterHookParameters> extends GlusterCommandBase<T> {

    @Inject
    private GlusterHooksDao glusterHooksDao;
    @Inject
    private GlusterUtil glusterUtil;

    protected GlusterHookEntity entity;

    public GlusterHookCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    public Cluster getCluster() {
        if (getGlusterHook() != null) {
            setClusterId(getGlusterHook().getClusterId());
        }
        return super.getCluster();
    }

    protected GlusterHookEntity getGlusterHook() {
        if (entity == null) {
            entity = glusterHooksDao.getById(getParameters().getHookId(), true);
        }
        return entity;
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (Guid.isNullOrEmpty(getParameters().getHookId())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_ID_IS_REQUIRED);
            return false;
        }

        if (glusterHooksDao.getById(getParameters().getHookId()) == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_HOOK_DOES_NOT_EXIST);
            return false;
        }

        return true;
    }

    protected List<VDS> getAllUpServers(Guid clusterId) {
        return glusterUtil.getAllUpServers(clusterId);
    }

    protected void updateServerHookStatusInDb(Guid hookId, Guid serverId, GlusterHookStatus status) {
        glusterHooksDao.updateGlusterServerHookStatus(hookId, serverId, status);
    }

    protected void updateHookInDb(GlusterHookEntity hook) {
        glusterHooksDao.updateGlusterHook(hook);
    }

    protected void addServerHookInDb(GlusterServerHook serverHook) {
        glusterHooksDao.saveGlusterServerHook(serverHook);
    }

    protected void updateGlusterHook(GlusterHookEntity entity) {
        if (entity.getConflictStatus() == 0) {
            glusterHooksDao.removeGlusterServerHooks(entity.getId());
        }
        glusterHooksDao.updateGlusterHook(entity);

    }

}
