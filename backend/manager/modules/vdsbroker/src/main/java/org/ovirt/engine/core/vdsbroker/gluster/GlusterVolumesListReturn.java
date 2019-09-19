package org.ovirt.engine.core.vdsbroker.gluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The return type to receive a list of gluster volumes. The constructor takes cluster id as well, so that
 * correct host can be identified when populating the bricks of a volume
 */
public final class GlusterVolumesListReturn extends StatusReturn {
    private static final String VOLUMES = "volumes";
    private static final String VOLUME_NAME = "volumeName";
    private static final String UUID = "uuid";
    private static final String VOLUME_TYPE = "volumeType";
    private static final String TRANSPORT_TYPE = "transportType";
    private static final String VOLUME_STATUS = "volumeStatus";
    private static final String BRICKS = "bricks";
    private static final String OPTIONS = "options";
    private static final String VOLUME_STATUS_ONLINE = "ONLINE";
    private static final String REPLICA_COUNT = "replicaCount";
    private static final String STRIPE_COUNT = "stripeCount";
    private static final String DISPERSE_COUNT = "disperseCount";
    private static final String REDUNDANCY_COUNT = "redundancyCount";
    private static final String BRICKS_INFO = "bricksInfo"; //contains brick name and server uuid
    private static final String IS_ARBITER = "isArbiter";
    private static final String NAME = "name";
    private static final String HOST_UUID = "hostUuid";

    private static final Logger log = LoggerFactory.getLogger(GlusterVolumesListReturn.class);
    private static final GlusterDBUtils dbUtils = Injector.get(GlusterDBUtils.class);

    private Guid clusterId;
    private final Map<Guid, GlusterVolumeEntity> volumes = new HashMap<>();

    @SuppressWarnings("unchecked")
    public GlusterVolumesListReturn(Guid clusterId, Map<String, Object> innerMap) {
        super(innerMap);
        this.clusterId = clusterId;

        if(getStatus().code != 0) {
            return;
        }

        Map<String, Object> volumesMap = (Map<String, Object>) innerMap.get(VOLUMES);

        for (Entry<String, Object> entry : volumesMap.entrySet()) {
            log.debug("received volume '{}'", entry.getKey());

            GlusterVolumeEntity volume = getVolume((Map<String, Object>)entry.getValue());
            volumes.put(volume.getId(), volume);
        }
    }

    @SuppressWarnings({ "unchecked", "incomplete-switch" })
    private GlusterVolumeEntity getVolume(Map<String, Object> map) {
        GlusterVolumeEntity volume = new GlusterVolumeEntity();

        volume.setClusterId(clusterId);
        volume.setId(Guid.createGuidFromStringDefaultEmpty((String)map.get(UUID)));
        volume.setName((String)map.get(VOLUME_NAME));
        volume.setVolumeType((String)map.get(VOLUME_TYPE));

        if (volume.getVolumeType() !=null) {
            if (volume.getVolumeType().isReplicatedType()) {
                volume.setReplicaCount(Integer.valueOf((String) map.get(REPLICA_COUNT)));
                boolean isArbiter = map.containsKey(IS_ARBITER)
                        ? Boolean.valueOf(map.get(IS_ARBITER).toString()) : Boolean.FALSE;
                volume.setIsArbiter(isArbiter);
            }
            if (volume.getVolumeType().isStripedType()) {
                volume.setStripeCount(Integer.valueOf((String) map.get(STRIPE_COUNT)));
            }
            if (volume.getVolumeType().isDispersedType()) {
                volume.setDisperseCount(Integer.valueOf((String) map.get(DISPERSE_COUNT)));
                volume.setRedundancyCount(Integer.valueOf((String) map.get(REDUNDANCY_COUNT)));
            }
        }
        for(Object transportType : (Object[])map.get(TRANSPORT_TYPE)) {
            volume.addTransportType(TransportType.valueOf((String)transportType));
        }

        String volStatus = (String)map.get(VOLUME_STATUS);
        if(volStatus.toUpperCase().equals(VOLUME_STATUS_ONLINE)) {
            volume.setStatus(GlusterStatus.UP);
        } else {
            volume.setStatus(GlusterStatus.DOWN);
        }

        try {
            if (map.get(BRICKS_INFO) != null && ((Object[])map.get(BRICKS_INFO)).length > 0) {
                volume.setBricks(getBricks(volume.getId(), (Object[])map.get(BRICKS_INFO), true));
            } else {
                volume.setBricks(getBricks(volume.getId(), (Object[])map.get(BRICKS), false));
            }
        } catch (Exception e) {
            log.error("Could not populate bricks of volume '{}' on cluster '{}': {}", volume.getName(), clusterId, e.getMessage());
            log.debug("Exception", e);
        }
        volume.setOptions(getOptions((Map<String, Object>)map.get(OPTIONS)));

        return volume;
    }

    private Map<String, String> getOptions(Map<String, Object> map) {
        Map<String, String> options = new HashMap<>();
        for(Entry<String, Object> entry : map.entrySet()) {
            options.put(entry.getKey(), (String)entry.getValue());
        }
        return options;
    }

    /**
     * Gets list of bricks of the volume from given list of brick representations. This can return null in certain cases
     * of failure e.g. if the brick representation contains an ip address which is mapped to more than servers in the
     * database.
     */
    private List<GlusterBrickEntity> getBricks(Guid volumeId, Object[] brickList, boolean withUuid) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity fetchedBrick;
        int brickOrder = 0;

        try {
            for (Object brick : brickList) {
                if (withUuid) {
                    fetchedBrick = getBrick(clusterId, volumeId, (Map<String, Object>) brick, brickOrder++);
                } else {
                    fetchedBrick = getBrick(clusterId, volumeId, (String) brick, brickOrder++);
                }
                if (fetchedBrick != null) {
                    bricks.add(fetchedBrick);
                }
            }
        } catch (Exception e) {
            // We do not want the command to fail if bricks for one of the volumes could not be fetched. Hence log the
            // exception and return null. The client should have special handling if bricks list of any of the volumes
            // is null.
            log.error("Error while populating bricks of volume '{}': {}", volumeId, e.getMessage());
            log.debug("Exception", e);
            return null;
        }

        return bricks;
    }

    /**
     * Returns a brick object for given cluster and brick representation of the form hostnameOrIp:brickDir
     * @param clusterId ID of the Cluster to which the brick belongs
     * @param volumeId ID of the Volume to which the brick belongs
     * @param brickInfo brick representation of the form hostnameOrIp:brickDir
     * @param brickOrder Order number of the brick
     * @return The brick object if representation passed is valid
     */
    private GlusterBrickEntity getBrick(Guid clusterId, Guid volumeId, String brickInfo, int brickOrder) {
        String[] brickParts = brickInfo.split(":", -1);
        if (brickParts.length != 2) {
            throw new RuntimeException("Invalid brick representation [" + brickInfo + "]");
        }

        String hostnameOrIp = brickParts[0];
        String brickDir = brickParts[1];

        VdsStatic server = dbUtils.getServer(clusterId, hostnameOrIp);
        if (server == null) {
            log.warn("Could not add brick '{}' to volume '{}' - server '{}' not found in cluster '{}'", brickInfo, volumeId, hostnameOrIp, clusterId);
            return null;
        }

        return getBrickEntity(clusterId, volumeId, brickOrder, server, brickDir, null, null, false);
    }

    private GlusterBrickEntity getBrick(Guid clusterId, Guid volumeId, Map<String, Object> brickInfoMap, int brickOrder) {
        String brickName = (String) brickInfoMap.get(NAME);

        String[] brickParts = brickName.split(":", -1);
        if (brickParts.length != 2) {
            throw new RuntimeException("Invalid brick representation [" + brickName + "]");
        }

        String hostUuid = (String) brickInfoMap.get(HOST_UUID);
        String brickDir = brickParts[1];
        String hostAddress = brickParts[0];
        boolean isArbiter = brickInfoMap.containsKey(IS_ARBITER)
                ? Boolean.valueOf(brickInfoMap.get(IS_ARBITER).toString()) : Boolean.FALSE;

        GlusterServer glusterServer = dbUtils.getServerByUuid(Guid.createGuidFromString(hostUuid));
        if (glusterServer == null) {
            log.warn("Could not add brick '{}' to volume '{}' - server uuid '{}' not found in cluster '{}'", brickName, volumeId, hostUuid, clusterId);
            return null;
        }
        VdsStatic server = Injector.get(VdsStaticDao.class).get(glusterServer.getId());
        String networkAddress = null;
        Guid networkId = null;
        if (!server.getHostName().equals(hostAddress)) {
            networkAddress = hostAddress;
            Network network = getGlusterNetworkId(server, networkAddress);
            if (network != null) {
                networkId = network.getId();
            } else {
                log.warn("Could not associate brick '{}' of volume '{}' with correct network as no gluster network found in cluster '{}'",
                        brickName,
                        volumeId,
                        clusterId);
            }
        }
        return getBrickEntity(clusterId, volumeId, brickOrder, server, brickDir, networkAddress, networkId, isArbiter);
    }

    private GlusterBrickEntity getBrickEntity(Guid clusterId,
            Guid volumeId,
            int brickOrder,
            VdsStatic server,
            String brickDir,
            String networkAddress,
            Guid networkId,
            boolean isArbiter) {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setVolumeId(volumeId);
        brick.setBrickOrder(brickOrder);
        brick.setBrickDirectory(brickDir);
        brick.setIsArbiter(isArbiter);

        brick.setServerId(server.getId());
        brick.setServerName(server.getHostName());

        brick.setNetworkAddress(networkAddress);
        brick.setNetworkId(networkId);

        return brick;
    }

    private Network getGlusterNetworkId(VdsStatic server, String networkAddress) {
        List<Network> allNetworksInCluster = Injector.get(NetworkDao.class).getAllForCluster(server.getClusterId());

        for (Network network : allNetworksInCluster) {
            if (network.getCluster().isGluster()
                    && isSameNetworkAddress(server.getId(), network.getName(), networkAddress)) {
                return network;
            }
        }
        return null;
    }

    private Boolean isSameNetworkAddress(Guid hostId, String glusterNetworkName, String networkAddress) {
        final List<VdsNetworkInterface> nics = Injector.get(InterfaceDao.class).getAllInterfacesForVds(hostId);
        String brickAddress = null;
        try {
            brickAddress = InetAddress.getByName(networkAddress).getHostAddress();
        } catch (UnknownHostException e) {
            return false;
        }

        for (VdsNetworkInterface nic : nics) {
            if (glusterNetworkName.equals(nic.getNetworkName())) {
                return brickAddress.equals(nic.getIpv4Address());
            }
        }
        return false;
    }

    public Map<Guid, GlusterVolumeEntity> getVolumes() {
        return volumes;
    }
}
