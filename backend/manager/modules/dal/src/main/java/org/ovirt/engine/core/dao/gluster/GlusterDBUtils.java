package org.ovirt.engine.core.dao.gluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class GlusterDBUtils {
    private static GlusterDBUtils instance = new GlusterDBUtils();

    public static GlusterDBUtils getInstance() {
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

    private GlusterServerDao getGlusterServerDao() {
        return getDbFacade().getGlusterServerDao();
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

    private VdsStaticDAO getVdsStaticDao() {
        return DbFacade.getInstance().getVdsStaticDao();
    }

    private InterfaceDao getInterfaceDao() {
        return DbFacade.getInstance().getInterfaceDao();
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
        return getGlusterServerDao().getByGlusterServerUuid(uuid);
    }

    /**
     * Returns a server from the given cluster, having give host name or IP address.
     *
     * @return VDS object for the server if found, else null
     */
    public VdsStatic getServer(Guid clusterId, String hostnameOrIp) {
        // first check for hostname
        VdsStatic server = getVdsStaticDao().getByHostName(hostnameOrIp);
        if (server != null) {
            return server.getVdsGroupId().equals(clusterId) ? server : null;
        }

        // then for ip
        List<VdsNetworkInterface> ifaces;
        try {
            ifaces =
                    getInterfaceDao().getAllInterfacesWithIpAddress(clusterId,
                            InetAddress.getByName(hostnameOrIp).getHostAddress());
            switch (ifaces.size()) {
            case 0:
                // not found
                return null;
            case 1:
                return getVdsStaticDao().get(ifaces.get(0).getVdsId());
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
        getGlusterBrickDao().removeAllInBatch(brickList);

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
            getGlusterVolumeDao().updateGlusterVolume(volume);
        }

        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPE
                && volume.getStripeCount() == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.STRIPE);
            getGlusterVolumeDao().updateGlusterVolume(volume);
        }

        if (volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_STRIPED_REPLICATE
                && (volume.getStripeCount() * volume.getReplicaCount()) == (volume.getBricks().size() - brickList.size())) {
            volume.setVolumeType(GlusterVolumeType.STRIPED_REPLICATE);
            getGlusterVolumeDao().updateGlusterVolume(volume);
        }
    }

    public String getHostNameOrIP(Guid glusterHostUuid) {
        String hostName = null;
        if (glusterHostUuid != null) {
            GlusterServer glusterServer = getGlusterServerDao().getByGlusterServerUuid(glusterHostUuid);
            if(glusterServer != null) {
                VdsStatic vds = getVdsStaticDao().get(glusterServer.getId());
                if(vds != null) {
                    hostName = vds.getHostName();
                }
            }
        }
        return hostName;
    }

    public GlusterBrickEntity getGlusterBrickByServerUuidAndBrickDir(Guid serverId, String brickDir)
    {
        return getGlusterBrickDao().getBrickByServerIdAndDirectory(serverId, brickDir);
    }

    public GlusterVolumeEntity getVolumeByNameAndHostId(String volumeName, Guid hostId) {
        VdsStatic vds = getVdsStaticDao().get(hostId);
        Guid vdsGroupId = vds.getVdsGroupId();
        return getGlusterVolumeDao().getByName(vdsGroupId, volumeName);
    }

}
