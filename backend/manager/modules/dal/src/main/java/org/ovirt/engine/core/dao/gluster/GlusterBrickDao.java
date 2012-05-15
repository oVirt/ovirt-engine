package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;

/**
 * Interface for DB operations on Gluster Bricks.
 */
public interface GlusterBrickDao extends DAO {
    public void save(GlusterBrickEntity brick);

    public GlusterBrickEntity getById(Guid id);

    public List<GlusterBrickEntity> getBricksOfVolume(Guid volumeId);

    public void removeBrick(Guid brickId);

    public void replaceBrick(GlusterBrickEntity oldBrick, GlusterBrickEntity newBrick);

    public void updateBrickStatus(Guid brickId, GlusterBrickStatus status);
}
