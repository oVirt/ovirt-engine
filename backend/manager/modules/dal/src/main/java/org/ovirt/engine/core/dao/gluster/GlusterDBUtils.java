package org.ovirt.engine.core.dao.gluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

@Singleton
public class GlusterDBUtils {
    @Inject
    private GlusterBrickDao glusterBrickDao;

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    @Inject
    private GlusterServerDao glusterServerDao;

    @Inject
    private GlusterVolumeSnapshotConfigDao glusterVolumeSnapshotConfigDao;

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private InterfaceDao interfaceDao;

    public boolean hasBricks(Guid serverId) {
        return glusterBrickDao.getGlusterVolumeBricksByServerId(serverId).size() > 0;
    }

    /**
     * Update status of all bricks of the given volume to the new status
     */
    public void updateBricksStatuses(Guid volumeId, GlusterStatus newStatus) {
        for (GlusterBrickEntity brick : glusterBrickDao.getBricksOfVolume(volumeId)) {
            glusterBrickDao.updateBrickStatus(brick.getId(), newStatus);
        }
    }

    /**
     * Update status of the given volume to the new status. This internally updates statuses of all bricks of the volume
     * as well.
     */
    public void updateVolumeStatus(Guid volumeId, GlusterStatus newStatus) {
        glusterVolumeDao.updateVolumeStatus(volumeId, newStatus);
        // When a volume goes UP or DOWN, all it's bricks should also be updated with the new status.
        updateBricksStatuses(volumeId, newStatus);
    }

    public boolean serverExists(Guid clusterId, String hostnameOrIp) {
        return getServer(clusterId, hostnameOrIp) != null;
    }

    public boolean serverExists(Guid uuid) {
        return getServerByUuid(uuid) != null;
    }

    /**
     * Returns a server from the given cluster, having given
     * gluster server UUID
     *
     * @return GlusterServer object for the said server if found, else null
     */
    public GlusterServer getServerByUuid(Guid uuid) {
        return glusterServerDao.getByGlusterServerUuid(uuid);
    }

    /**
     * Returns a server from the given cluster, having give host name or IP address.
     *
     * @return VDS object for the server if found, else null
     */
    public VdsStatic getServer(Guid clusterId, String hostnameOrIp) {
        // first check for hostname
        VdsStatic server = vdsStaticDao.getByHostName(hostnameOrIp);
        if (server != null) {
            return server.getClusterId().equals(clusterId) ? server : null;
        }

        // then for ip
        List<VdsNetworkInterface> ifaces;
        try {
            ifaces =
                    interfaceDao.getAllInterfacesWithIpAddress(clusterId,
                            InetAddress.getByName(hostnameOrIp).getHostAddress());
            switch (ifaces.size()) {
            case 0:
                // not found
                return null;
            case 1:
                return vdsStaticDao.get(ifaces.get(0).getVdsId());
            default:
                // multiple servers in the DB having this ip address!
                throw new RuntimeException("There are multiple servers in DB having same IP address " + hostnameOrIp);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeBricksFromVolumeInDb(GlusterVolumeEntity volume,
            List<GlusterBrickEntity> brickList,
            int volumeReplicaCount) {
        glusterBrickDao.removeAllInBatch(brickList);

        // Update volume type and replica/stripe count
        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE
                && volume.getReplicaCount() == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.REPLICATE);
        }
        if (volume.getVolumeType().isReplicatedType()) {
            int replicaCount =
                    (volumeReplicaCount == 0)
                            ? volume.getReplicaCount()
                            : volumeReplicaCount;
            volume.setReplicaCount(replicaCount);
            glusterVolumeDao.updateGlusterVolume(volume);
        }

        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE
                && volume.getStripeCount() == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.STRIPE);
            glusterVolumeDao.updateGlusterVolume(volume);
        }

        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPED_REPLICATE
                && (volume.getStripeCount() * volume.getReplicaCount()) == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.STRIPED_REPLICATE);
            glusterVolumeDao.updateGlusterVolume(volume);
        }
    }

    public String getHostNameOrIP(Guid glusterHostUuid) {
        String hostName = null;
        if (glusterHostUuid != null) {
            GlusterServer glusterServer = glusterServerDao.getByGlusterServerUuid(glusterHostUuid);
            if(glusterServer != null) {
                VdsStatic vds = vdsStaticDao.get(glusterServer.getId());
                if(vds != null) {
                    hostName = vds.getHostName();
                }
            }
        }
        return hostName;
    }

    public GlusterBrickEntity getGlusterBrickByServerUuidAndBrickDir(Guid serverId, String brickDir) {
        return glusterBrickDao.getBrickByServerIdAndDirectory(serverId, brickDir);
    }

    public boolean isVolumeSnapshotSoftLimitReached(Guid volumeId) {
        GlusterVolumeEntity volume = glusterVolumeDao.getById(volumeId);

        if (volume != null) {
            GlusterVolumeSnapshotConfig config =
                    glusterVolumeSnapshotConfigDao.getConfigByClusterIdAndName(volume.getClusterId(),
                            "snap-max-soft-limit");

            if (config != null) {
                // remove the % sign in the last
                String configValue = StringUtils.removeEnd(config.getParamValue(), "%");
                int snapMaxSoftLimitPcnt = Integer.parseInt(configValue);

                int snapshotCount = volume.getSnapshotsCount();
                int snapMaxLimit = volume.getSnapMaxLimit();

                return snapshotCount >= (snapMaxLimit * snapMaxSoftLimitPcnt) / 100;
            }
        }

        return false;
    }

    public boolean isVolumeSnapshotHardLimitReached(Guid volumeId) {
        GlusterVolumeEntity volume = glusterVolumeDao.getById(volumeId);

        if (volume != null) {
            GlusterVolumeSnapshotConfig config =
                    glusterVolumeSnapshotConfigDao.getConfigByVolumeIdAndName(volume.getClusterId(),
                            volumeId,
                            "snap-max-hard-limit");

            if (config != null) {
                int snapMaxHardLimit = Integer.parseInt(config.getParamValue());
                int snapshotCount = volume.getSnapshotsCount();

                return snapshotCount >= snapMaxHardLimit;
            }
        }

        return false;
    }

    public GlusterVolumeEntity getGlusterVolInfoByClusterIdAndVolName(Guid clusterId, String volumeName) {
        return glusterVolumeDao.getByName(clusterId, volumeName);
    }

}
