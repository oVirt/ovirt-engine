package org.ovirt.engine.ui.frontend.utils;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;

public class GlusterVolumeUtils {
    public static VolumeStatus getVolumeStatus(GlusterVolumeEntity volume) {
        GlusterStatus status = volume.getStatus();
        int count = 0;
        int brickCount = volume.getBricks().size();
        switch (status) {
        case DOWN :
            return VolumeStatus.DOWN;
        case UP :
            count = countDownBricks(volume);
            if (count == 0) {
                return VolumeStatus.UP;
            } else if (count < brickCount) {
                return VolumeStatus.SOME_BRICKS_DOWN;
            } else {
                return VolumeStatus.ALL_BRICKS_DOWN;
            }
        default :
            return VolumeStatus.DOWN;
        }
    }

    public static int countDownBricks(GlusterVolumeEntity volume) {
        int downCount = 0;
        int upCount = 0;
        for (GlusterBrickEntity brick : volume.getBricks()) {
            if (brick.getStatus() == GlusterStatus.UP) {
                upCount++;
            } else {
                downCount++;
            }
            if (upCount > 0 && downCount > 0) {
                return downCount;
            }
        }
        return downCount;
    }

    public static boolean isHealingRequired(GlusterVolumeEntity volume) {
        for (GlusterBrickEntity brick : volume.getBricks()) {
            if (brick.getUnSyncedEntries() != null && brick.getUnSyncedEntries() > 0) {
                return true;
            }
        }
        return false;
    }

    public static enum VolumeStatus {
        UP,
        SOME_BRICKS_DOWN,
        ALL_BRICKS_DOWN,
        DOWN;
    };

}
