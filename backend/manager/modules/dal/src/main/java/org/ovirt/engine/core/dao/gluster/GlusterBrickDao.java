package org.ovirt.engine.core.dao.gluster;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.MassOperationsDao;

/**
 * Interface for DB operations on Gluster Bricks.
 */
public interface GlusterBrickDao extends Dao, MassOperationsDao<GlusterBrickEntity, Guid> {
    public void save(GlusterBrickEntity brick);

    public GlusterBrickEntity getById(Guid id);

    public List<GlusterBrickEntity> getBricksOfVolume(Guid volumeId);

    public void removeBrick(Guid brickId);

    public void replaceBrick(GlusterBrickEntity oldBrick, GlusterBrickEntity newBrick);

    public void updateBrickStatus(Guid brickId, GlusterStatus status);

    public void updateBrickStatuses(List<GlusterBrickEntity> bricks);

    public void updateBrickOrder(Guid brickId, int brickOrder);

    public List<GlusterBrickEntity> getGlusterVolumeBricksByServerId(Guid serverId);

    public GlusterBrickEntity getBrickByServerIdAndDirectory(Guid serverId, String brickDirectory);

    public List<GlusterBrickEntity> getGlusterVolumeBricksByTaskId(Guid taskId);

    public void updateBrickTask(Guid brickId, Guid taskId);

    public void updateBrickTasksInBatch(Collection<GlusterBrickEntity> bricks);

    /**
     * Updates the given task id on brick identified by server id and brick dir parameters
     */
    public void updateBrickTaskByHostIdBrickDir(Guid serverId, String brickDir, Guid taskId);

    /**
     * Updates the collection of brick entities with task id populated in entity, in a batch.
     * Each brick is identified by server id and brick dir populated in the brick entity.
     */
    public void updateAllBrickTasksByHostIdBrickDirInBatch(Collection<GlusterBrickEntity> bricks);

    public void addBrickProperties(BrickProperties brickProperties);

    public void addBrickProperties(List<GlusterBrickEntity> bricks);

    public void updateBrickProperties(BrickProperties brickProperties);

    public void updateBrickProperties(List<GlusterBrickEntity> bricks);

    public void updateBrickNetworkId(Guid brickId, Guid networkId);

    public List<GlusterBrickEntity> getAllByClusterAndNetworkId(Guid clusterId, Guid networkId);

    public void updateUnSyncedEntries(List<GlusterBrickEntity> bricks);
}
