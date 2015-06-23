package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@Singleton
public class GlusterHostValidator {

    private final GlusterBrickDao brickDao;
    private final GlusterVolumeDao volumeDao;

    @Inject
    public GlusterHostValidator(GlusterVolumeDao volumeDao, GlusterBrickDao brickDao){
        Validate.notNull(volumeDao, "volumeDao can not be null");
        Validate.notNull(brickDao, "brickDao can not be null");
        this.volumeDao = volumeDao;
        this.brickDao = brickDao;
    }

    private boolean isQuorumMet(GlusterVolumeEntity volume,
            List<GlusterBrickEntity> bricksGoingToMaintenance) {
        int replicaCount = volume.getReplicaCount();
        List<GlusterBrickEntity> bricks = volume.getBricks();
        int subVolumes = bricks.size() / replicaCount;
        String quorumType = volume.getOptionValue(GlusterConstants.OPTION_QUORUM_TYPE);
        int quorumCount;
        // Quorum count will be directly specified in case of quorum type 'fixed'.
        // Incase of quorum type 'auto', more than 50% bricks should be UP (e.g 2 out 3 bricks should be up in case of
        // replica 3 volume)

        if (GlusterConstants.OPTION_QUORUM_TYPE_FIXED.equals(quorumType)) {
            quorumCount = Integer.parseInt(volume.getOptionValue(GlusterConstants.OPTION_QUORUM_COUNT));
        } else if (GlusterConstants.OPTION_QUORUM_TYPE_AUTO.equals(quorumType)) {
            quorumCount = (int) Math.ceil((double) replicaCount / 2);
        } else {
            return true;
        }

        for (int index = 0; index < subVolumes; index++) {
            List<GlusterBrickEntity> bricksInSubVolume =
                    bricks.subList(index * replicaCount, (index * replicaCount) + replicaCount);

            int bricksGoingDown = 0;
            int remainingUpBricks = 0;
            for (GlusterBrickEntity brick : bricksInSubVolume) {
                if (GlusterStatus.UP.equals(brick.getStatus()) && bricksGoingToMaintenance.contains(brick)) {
                    bricksGoingDown++;
                } else if (GlusterStatus.UP.equals(brick.getStatus())) {
                    remainingUpBricks++;
                }
            }

            if (bricksGoingDown > 0) {
                if (remainingUpBricks < quorumCount) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> checkGlusterQuorum(Cluster cluster, Iterable<Guid> selectedHostIdsForMaintenance) {
        List<String> volumesWithoutQuorum = new ArrayList<>();
        if (cluster.supportsGlusterService()) {
            List<GlusterBrickEntity> bricksGoingToMaintenance = new ArrayList<>();
            for (Guid serverId : selectedHostIdsForMaintenance) {
                bricksGoingToMaintenance.addAll(brickDao.getGlusterVolumeBricksByServerId(serverId));
            }
            List<GlusterVolumeEntity> volumesInCluster = volumeDao.getByClusterId(cluster.getId());
            volumesWithoutQuorum = volumesInCluster.stream()
                    .filter(volume -> volume.getStatus() == GlusterStatus.UP
                            && volume.getVolumeType().isReplicatedType()
                            && !isQuorumMet(volume, bricksGoingToMaintenance))
                    .map(v -> v.getName())
                    .collect(Collectors.toList());
        }
        return volumesWithoutQuorum;
    }

    public Map<Guid, List<String>> checkUnsyncedEntries(Iterable<Guid> hostIds) {
        Map<Guid, List<String>> result = new HashMap<>();
        for (Guid serverId : hostIds) {
            List<GlusterBrickEntity> bricks = brickDao.getGlusterVolumeBricksByServerId(serverId);
            List<String> bricksWithUnsyncedEntries = bricks.stream()
                    .filter(brick -> brick.getStatus() == GlusterStatus.UP && brick.getUnSyncedEntries() != null
                            && brick.getUnSyncedEntries() > 0)
                    .map(brick -> brick.getQualifiedName())
                    .collect(Collectors.toList());
            if (!bricksWithUnsyncedEntries.isEmpty()) {
                result.put(serverId, bricksWithUnsyncedEntries);
            }
        }
        return result;
    }
}
