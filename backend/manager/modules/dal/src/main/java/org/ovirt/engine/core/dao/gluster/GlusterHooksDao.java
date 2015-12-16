package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStage;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.MassOperationsDao;
import org.ovirt.engine.core.dao.SearchDao;

/**
 * Interface for DB operations on Gluster Hooks.
 */
public interface GlusterHooksDao extends Dao, SearchDao<GlusterHookEntity>, MassOperationsDao<GlusterHookEntity, Guid> {

    public void save(GlusterHookEntity glusterHook);

    /**
     * Retrieves the GlusterHookEntity. By default the content and list of GlusterServerHooks are not loaded.
     * Use {@link GlusterHooksDao#getById(Guid, boolean)} for this.
     */
    public GlusterHookEntity getById(Guid id);

    /**
     * Retrieves the GlusterHook.
     * If eagerLoad is set to true, the content and related GlusterServerHook entities are
     * retrieved too.
     * @param eagerLoad - indicates if related entities are populated too.
     */
    public GlusterHookEntity getById(Guid id, boolean eagerLoad);

    public GlusterHookEntity getGlusterHook(Guid clusterId, String glusterCommand, GlusterHookStage stage, String hookName);

    public List<GlusterServerHook> getGlusterServerHooks(Guid hookId);

    public GlusterServerHook getGlusterServerHook(Guid hookId, Guid serverId);

    public List<GlusterHookEntity> getByClusterId(Guid clusterId);

    public String getGlusterHookContent(Guid glusterHookId);

    @Override
    public List<GlusterHookEntity> getAllWithQuery(String query);

    public void remove(Guid id);

    public void updateGlusterHook(GlusterHookEntity hook);

    public void updateGlusterHookStatus(Guid hookId, GlusterHookStatus status);

    public void updateGlusterServerHookStatus(Guid hookId, Guid serverId, GlusterHookStatus status);

    public void updateGlusterHookContent(Guid id, String checksum, String content);

    public void updateGlusterHookConflictStatus(Guid hookId, Integer conflictStatus);

    public void saveOrUpdateGlusterServerHook(GlusterServerHook serverHook);

    public void saveGlusterServerHook(GlusterServerHook serverHook);

    public void updateGlusterServerHookChecksum(Guid hookId, Guid serverId, String checksum);

    public void updateGlusterServerHook(GlusterServerHook serverHook);

    public void removeGlusterServerHooks(Guid hookId);

    public void removeGlusterServerHook(Guid hookId, Guid serverId);

    public void removeAllInCluster(Guid clusterId);

}
