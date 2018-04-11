package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClientInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.core.common.businessentities.gluster.MemoryStatus;
import org.ovirt.engine.core.common.businessentities.gluster.Mempool;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class GlusterVolumeStatusReturn extends StatusReturn {
    private static final String STATUS = "status";
    private static final String VOLUME_STATUS = "volumeStatus";
    private static final String VOLUME_STATUS_INFO = "volumeStatsInfo";
    private static final String VOLUME_NAME = "name";
    private static final String PORT = "port";
    private static final String RDMA_PORT = "rdma_port";
    private static final String PID = "pid";
    private static final String ONLINE = "ONLINE";
    private static final String BRICKS = "bricks";
    private static final String BRICK = "brick";
    private static final String NFS_KEY = "nfs";
    private static final String SHD_KEY = "shd";
    private static final String HOSTNAME = "hostname";
    private static final String HOST_UUID = "hostuuid";

    private static final String DETAIL_SIZE_TOTAL = "sizeTotal";
    private static final String DETAIL_SIZE_FREE = "sizeFree";
    private static final String DETAIL_SIZE_USED = "sizeUsed";
    private static final String DETAIL_DEVICE = "device";
    private static final String DETAIL_BLOCK_SIZE = "blockSize";
    private static final String DETAIL_MNT_OPTIONS = "mntOptions";
    private static final String DETAIL_FS_NAME = "fsName";

    private static final String CLIENTS_STATUS = "clientsStatus";
    private static final String CLIENTS_HOST_NAME = "hostname";
    private static final String CLIENTS_BYTES_READ = "bytesRead";
    private static final String CLIENTS_BYTES_WRITE = "bytesWrite";

    private static final String MEMORY_MALL_INFO = "mallinfo";
    private static final String MEMORY_ARENA = "arena";
    private static final String MEMORY_ORDBLKS = "ordblks";
    private static final String MEMORY_SMBLKS = "smblks";
    private static final String MEMORY_HBLKS = "hblks";
    private static final String MEMORY_HBLKHD = "hblkhd";
    private static final String MEMORY_USMBLKS = "usmblks";
    private static final String MEMORY_FSMBLKS = "fsmblks";
    private static final String MEMORY_UORDBLKS = "uordblks";
    private static final String MEMORY_FORDBLKS = "fordblks";
    private static final String MEMORY_KEEPCOST = "keepcost";

    private static final String MEMORY_MEM_POOL = "mempool";
    private static final String MEMORY_NAME = "name";
    private static final String MEMORY_HOTCOUNT = "hotCount";
    private static final String MEMORY_COLDCOUNT = "coldCount";
    private static final String MEMORY_PADDDEDSIZEOF = "padddedSizeOf";
    private static final String MEMORY_ALLOCCOUNT = "allocCount";
    private static final String MEMORY_MAXALLOC = "maxAlloc";
    private static final String MEMORY_POOLMISSES = "poolMisses";
    private static final String MEMORY_MAXSTDALLOC = "maxStdAlloc";

    private static final Logger log = LoggerFactory.getLogger(GlusterVolumeStatusReturn.class);
    private Status status;
    private final GlusterVolumeAdvancedDetails volumeAdvancedDetails = new GlusterVolumeAdvancedDetails();

    public GlusterVolumeStatusReturn(Guid clusterId, Map<String, Object> innerMap) {
        super(innerMap);
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        Map<String, Object> statusInfo = (Map<String, Object>) innerMap.get(VOLUME_STATUS);

        if (statusInfo != null) {
            String volumeName = (String) statusInfo.get(VOLUME_NAME);
            GlusterVolumeEntity volume = getGlusterVolumeDao().getByName(clusterId, volumeName);

            volumeAdvancedDetails.setVolumeId(volume.getId());
            List<BrickDetails> brickDetails = prepareBrickDetails(volume, (Object[]) statusInfo.get(BRICKS));
            volumeAdvancedDetails.setBrickDetails(brickDetails);
            volumeAdvancedDetails.setServiceInfo(prepareServiceInfo(statusInfo));
            GlusterVolumeSizeInfo capacityInfo = null;

            // Fetch the volume capacity detail
            if (statusInfo.containsKey(VOLUME_STATUS_INFO)) {
                Map<String, Object> volumeStatusInfo = (Map<String, Object>) statusInfo.get("volumeStatsInfo");
                capacityInfo = new GlusterVolumeSizeInfo();
                capacityInfo.setVolumeId(volume.getId());
                capacityInfo.setTotalSize(Long.parseLong((String) volumeStatusInfo.get(DETAIL_SIZE_TOTAL)));
                capacityInfo.setUsedSize(Long.parseLong((String) volumeStatusInfo.get(DETAIL_SIZE_USED)));
                capacityInfo.setFreeSize(Long.parseLong((String) volumeStatusInfo.get(DETAIL_SIZE_FREE)));
                volumeAdvancedDetails.setCapacityInfo(capacityInfo);
            }
        }
    }

    private List<GlusterServerService> prepareServiceInfo(Map<String, Object> statusInfo) {
        List<GlusterServerService> serviceInfoList = new ArrayList<>();
        prepareServiceInfo(statusInfo, serviceInfoList, NFS_KEY);
        prepareServiceInfo(statusInfo, serviceInfoList, SHD_KEY);
        return serviceInfoList;
    }

    private void prepareServiceInfo(Map<String, Object> statusInfo, List<GlusterServerService> serviceInfoList, String service) {
        if (statusInfo.containsKey(service)) {
            Object[] serviceInfo = (Object[]) statusInfo.get(service);
            for (Object serviceObj : serviceInfo) {
                Map<String, Object> serviceMap = (Map<String, Object>) serviceObj;
                GlusterServerService parsedServiceInfo = parseServiceInfo(serviceMap);
                parsedServiceInfo.setServiceType(service.equals(NFS_KEY) ? ServiceType.NFS : ServiceType.SHD);
                serviceInfoList.add(parsedServiceInfo);
            }
        }
    }

    private GlusterServerService parseServiceInfo(Map<String, Object> volumeServiceInfo) {
        GlusterServerService serviceInfo = new GlusterServerService();

        if (volumeServiceInfo.containsKey(HOSTNAME)) {
            serviceInfo.setHostName((String) volumeServiceInfo.get(HOSTNAME));
        }

        if (volumeServiceInfo.containsKey(HOST_UUID)) {
            serviceInfo.setGlusterHostUuid(Guid.createGuidFromString((String) volumeServiceInfo.get(HOST_UUID)));
        }

        if (volumeServiceInfo.containsKey(STATUS)) {
            String brickStatus = (String) volumeServiceInfo.get(STATUS);
            if (brickStatus.toUpperCase().equals(ONLINE)) {
                serviceInfo.setStatus(GlusterServiceStatus.RUNNING);
                // parse the port and pid only if the service is running.
                if (volumeServiceInfo.containsKey(PORT) && StringUtils.isNumeric((String)volumeServiceInfo.get(PORT))) {
                    serviceInfo.setPort(Integer.parseInt((String) volumeServiceInfo.get(PORT)));
                }

                if (volumeServiceInfo.containsKey(RDMA_PORT) && StringUtils.isNumeric((String)volumeServiceInfo.get(RDMA_PORT))) {
                    serviceInfo.setRdmaPort(Integer.parseInt((String) volumeServiceInfo.get(RDMA_PORT)));
                }

                if (volumeServiceInfo.containsKey(PID) && StringUtils.isNumeric((String)volumeServiceInfo.get(PID))) {
                    serviceInfo.setPid(Integer.parseInt((String) volumeServiceInfo.get(PID)));
                }
            } else {
                serviceInfo.setStatus(GlusterServiceStatus.STOPPED);
            }
        }
        return serviceInfo;
    }

    private List<BrickDetails> prepareBrickDetails(GlusterVolumeEntity volume, Object[] bricksList) {
        List<BrickDetails> brickDetailsList = new ArrayList<>();
        for (Object brickObj : bricksList) {
            BrickDetails brickDetails = new BrickDetails();
            Map<String, Object> brick = (Map<String, Object>) brickObj;
            brickDetails.setBrickProperties(getBrickProperties(volume, brick));

            // Fetch Clients Details
            if (brick.containsKey(CLIENTS_STATUS)) {
                brickDetails.setClients(prepareClientInfo((Object[]) brick.get(CLIENTS_STATUS)));
            }

            // Fetch Memory Details
            if (brick.containsKey(MEMORY_MALL_INFO) || brick.containsKey(MEMORY_MEM_POOL)) {
                MemoryStatus memoryStatus = new MemoryStatus();
                memoryStatus.setMallInfo(prepareMallInfo((Map<String, Object>) brick.get(MEMORY_MALL_INFO)));
                memoryStatus.setMemPools(prepareMemPool((Object[]) brick.get(MEMORY_MEM_POOL)));
                brickDetails.setMemoryStatus(memoryStatus);
            }

            brickDetailsList.add(brickDetails);
        }
        return brickDetailsList;
    }

    private BrickProperties getBrickProperties(GlusterVolumeEntity volume, Map<String, Object> brick) {
        BrickProperties brickProperties = new BrickProperties();
        GlusterBrickEntity brickEntity = getBrickEntity(volume, brick);

        if (brickEntity != null) {
            brickProperties.setBrickId(brickEntity.getId());
        } else {
            log.warn("Could not update brick '{}' as not found in db", brick.get(BRICK));
        }

        if (brick.containsKey(STATUS)) {
            String brickStatus = (String) brick.get(STATUS);
            if (brickStatus.toUpperCase().equals(ONLINE)) {
                brickProperties.setStatus(GlusterStatus.UP);
                boolean portPresent = false;
                if (brick.containsKey(PORT) && StringUtils.isNumeric((String)brick.get(PORT))) {
                    brickProperties.setPort(Integer.parseInt((String) brick.get(PORT)));
                    portPresent = true;
                }
                if (brick.containsKey(RDMA_PORT) && StringUtils.isNumeric((String)brick.get(RDMA_PORT))) {
                    brickProperties.setRdmaPort(Integer.parseInt((String) brick.get(RDMA_PORT)));
                    portPresent = true;
                }
                if (!portPresent) {
                    //if there's no port registered, then the brick status is down.
                    brickProperties.setStatus(GlusterStatus.DOWN);
                }

                if (brick.containsKey(PID) && StringUtils.isNumeric((String)brick.get(PID))) {
                    brickProperties.setPid(Integer.parseInt((String) brick.get(PID)));
                }
            } else {
                brickProperties.setStatus(GlusterStatus.DOWN);
            }
        }

        // Fetch the volume status detail
        if (brick.containsKey(DETAIL_SIZE_TOTAL)) {
            brickProperties.setTotalSize(Double.parseDouble((String) brick.get(DETAIL_SIZE_TOTAL)));
        }
        if (brick.containsKey(DETAIL_SIZE_FREE)) {
            brickProperties.setFreeSize(Double.parseDouble((String) brick.get(DETAIL_SIZE_FREE)));
        }

        if (brick.containsKey(DETAIL_DEVICE)) {
            brickProperties.setDevice((String) brick.get(DETAIL_DEVICE));
        }

        if (brick.containsKey(DETAIL_BLOCK_SIZE)) {
            brickProperties.setBlockSize(Integer.parseInt((String) brick.get(DETAIL_BLOCK_SIZE)));
        }

        if (brick.containsKey(DETAIL_MNT_OPTIONS)) {
            brickProperties.setMntOptions((String) brick.get(DETAIL_MNT_OPTIONS));
        }

        if (brick.containsKey(DETAIL_FS_NAME)) {
            brickProperties.setFsName((String) brick.get(DETAIL_FS_NAME));
        }
        return brickProperties;
    }

    private GlusterBrickEntity getBrickEntity(GlusterVolumeEntity volume, Map<String, Object> brick) {
        String brickName = (String) brick.get(BRICK);

        String glusterHostUuid = (String) brick.get(HOST_UUID);
        if (!StringUtils.isEmpty(glusterHostUuid)) {
            GlusterServer glusterServer =
                    Injector.get(GlusterDBUtils.class).getServerByUuid(Guid.createGuidFromString(glusterHostUuid));
            if (glusterServer == null) {
                log.warn("Could not update brick '{}' to volume '{}' - server uuid '{}' not found",
                        brickName, volume.getName(), glusterHostUuid);
                return null;
            }
            String[] brickParts = brickName.split(":", -1);
            if (brickParts.length != 2) {
                log.warn("Invalid brick representation '{}'", brickName);
                return null;
            }
            String brickDir = brickParts[1];
            return Injector.get(GlusterBrickDao.class).getBrickByServerIdAndDirectory(glusterServer.getId(), brickDir);
        }
        return GlusterCoreUtil.getBrickByQualifiedName(volume.getBricks(), brickName);
    }

    private List<Mempool> prepareMemPool(Object[] memoryPool) {
        List<Mempool> memPoolList = new ArrayList<>();
        for (Object memPoolObj : memoryPool) {
            Mempool glusterMemoryPool = new Mempool();
            Map<String, Object> memPool = (Map<String, Object>) memPoolObj;
            glusterMemoryPool.setName((String) memPool.get(MEMORY_NAME));
            glusterMemoryPool.setHotCount(Integer.parseInt((String) memPool.get(MEMORY_HOTCOUNT)));
            glusterMemoryPool.setColdCount(Integer.parseInt((String) memPool.get(MEMORY_COLDCOUNT)));
            glusterMemoryPool.setPadddedSize(Integer.parseInt((String) memPool.get(MEMORY_PADDDEDSIZEOF)));
            glusterMemoryPool.setAllocCount(Integer.parseInt((String) memPool.get(MEMORY_ALLOCCOUNT)));
            glusterMemoryPool.setMaxAlloc(Integer.parseInt((String) memPool.get(MEMORY_MAXALLOC)));
            glusterMemoryPool.setPoolMisses(Integer.parseInt((String) memPool.get(MEMORY_POOLMISSES)));
            glusterMemoryPool.setMaxStdAlloc(Integer.parseInt((String) memPool.get(MEMORY_MAXSTDALLOC)));
            memPoolList.add(glusterMemoryPool);
        }
        return memPoolList;
    }

    private MallInfo prepareMallInfo(Map<String, Object> mallInfo) {
        MallInfo glusterMallInfo = new MallInfo();
        glusterMallInfo.setArena(Integer.parseInt((String) mallInfo.get(MEMORY_ARENA)));
        glusterMallInfo.setOrdblks(Integer.parseInt((String) mallInfo.get(MEMORY_ORDBLKS)));
        glusterMallInfo.setSmblks(Integer.parseInt((String) mallInfo.get(MEMORY_SMBLKS)));
        glusterMallInfo.setHblks(Integer.parseInt((String) mallInfo.get(MEMORY_HBLKS)));
        glusterMallInfo.setHblkhd(Integer.parseInt((String) mallInfo.get(MEMORY_HBLKHD)));
        glusterMallInfo.setUsmblks(Integer.parseInt((String) mallInfo.get(MEMORY_USMBLKS)));
        glusterMallInfo.setFsmblks(Integer.parseInt((String) mallInfo.get(MEMORY_FSMBLKS)));
        glusterMallInfo.setUordblks(Integer.parseInt((String) mallInfo.get(MEMORY_UORDBLKS)));
        glusterMallInfo.setFordblks(Integer.parseInt((String) mallInfo.get(MEMORY_FORDBLKS)));
        glusterMallInfo.setKeepcost(Integer.parseInt((String) mallInfo.get(MEMORY_KEEPCOST)));
        return glusterMallInfo;
    }

    private List<GlusterClientInfo> prepareClientInfo(Object[] clientsStatus) {
        List<GlusterClientInfo> clientInfoList = new ArrayList<>();
        for (Object clientStatusObj : clientsStatus) {
            GlusterClientInfo clientInfo = new GlusterClientInfo();
            Map<String, Object> client = (Map<String, Object>) clientStatusObj;
            String hostName = (String) client.get(CLIENTS_HOST_NAME);
            String[] hostNameArr = hostName.split(":", -1);

            clientInfo.setHostname(hostNameArr[0]);
            clientInfo.setClientPort(Integer.parseInt(hostNameArr[1]));
            clientInfo.setBytesRead(Long.parseLong((String) client.get(CLIENTS_BYTES_READ)));
            clientInfo.setBytesWritten(Long.parseLong((String) client.get(CLIENTS_BYTES_WRITE)));
            clientInfoList.add(clientInfo);
        }
        return clientInfoList;
    }

    protected GlusterVolumeDao getGlusterVolumeDao() {
        return Injector.get(GlusterVolumeDao.class);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    public GlusterVolumeAdvancedDetails getVolumeAdvancedDetails() {
        return volumeAdvancedDetails;
    }

}
