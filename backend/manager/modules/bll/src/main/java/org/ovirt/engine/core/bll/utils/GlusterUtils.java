package org.ovirt.engine.core.bll.utils;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class GlusterUtils {
    private static GlusterUtils instance = new GlusterUtils();

    public static GlusterUtils getInstance() {
        return instance;
    }

    private DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    private GlusterBrickDao getGlusterBrickDao() {
        return getDbFacade().getGlusterBrickDao();
    }

    private GlusterVolumeDao getGlusterVolumeDao() {
        return getDbFacade().getGlusterVolumeDao();
    }

    public boolean hasBricks(Guid serverId) {
        return (getGlusterBrickDao().getGlusterVolumeBricksByServerId(serverId).size() > 0);
    }

    /**
     * Update status of all bricks of the given volume to the new status
     */
    public void updateBricksStatuses(Guid volumeId, GlusterStatus newStatus) {
        for (GlusterBrickEntity brick : getGlusterBrickDao().getBricksOfVolume(volumeId)) {
            getGlusterBrickDao().updateBrickStatus(brick.getId(), newStatus);
        }
    }

    /**
     * Update status of the given volume to the new status. This internally updates statuses of all bricks of the volume
     * as well.
     */
    public void updateVolumeStatus(Guid volumeId, GlusterStatus newStatus) {
        getGlusterVolumeDao().updateVolumeStatus(volumeId, newStatus);
        // When a volume goes UP or DOWN, all it's bricks should also be updated with the new status.
        updateBricksStatuses(volumeId, newStatus);
    }
}
