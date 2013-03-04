package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.MassOperationsDao;

/**
 * Interface for DB operations on Gluster Bricks.
 */
public interface GlusterBrickDao extends DAO, MassOperationsDao<GlusterBrickEntity, Guid> {
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
}
