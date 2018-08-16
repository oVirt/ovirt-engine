package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterLocalLogicalVolume;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterLocalPhysicalVolume;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterLocalVolumeInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVDOVolume;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides helper services for dealing with thin devices:
 * - Querying thin device data
 * - Calculating thin device totals
 * - Propagating thin device totals to the volumes/SDs
 * - Sending events
 * - etc
 */
@Singleton
public class GlusterThinDeviceService {
    private static final Logger log = LoggerFactory.getLogger(GlusterThinDeviceService.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private GlusterBrickDao brickDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    @Inject
    private StorageDomainDynamicDao storageDomainDynamicDao;

    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private VDSBrokerFrontend resourceManager;

    /**
     * Retrieves local volume information (LV,PV,VDO) for each host in a
     * specified cluter. Data is retrieved using VDSM calls.
     *
     * @param clusterId GUID of cluster to retrieve data.
     * @return map of GlusterLocalVolumeInfo for each host in the cluster, identified by host id.
     */
    public Map<Guid, GlusterLocalVolumeInfo> getLocalVolumeInfo(Guid clusterId) {
        Map<Guid, GlusterLocalVolumeInfo> localVolumeInfoMap = new HashMap<>();
        for (VDS vds : vdsDao.getAllForCluster(clusterId)) {
            if (vds.getStatus() != VDSStatus.Up) {
                continue;
            }
            try {
                log.debug("Getting LVM/VDO information for the host {}", vds.getName());
                GlusterLocalVolumeInfo localVolumeInfo = new GlusterLocalVolumeInfo();
                VDSReturnValue
                        logicalVolumesResult = resourceManager.runVdsCommand(
                        VDSCommandType.GetGlusterLocalLogicalVolumeList,
                        new VdsIdVDSCommandParametersBase(vds.getId()));
                if (logicalVolumesResult.getSucceeded()) {
                    localVolumeInfo.setLogicalVolumes((List<GlusterLocalLogicalVolume>) logicalVolumesResult.getReturnValue());
                }
                VDSReturnValue physicalVolumesResult = resourceManager.runVdsCommand(
                        VDSCommandType.GetGlusterLocalPhysicalVolumeList,
                        new VdsIdVDSCommandParametersBase(vds.getId()));
                if (physicalVolumesResult.getSucceeded()) {
                    localVolumeInfo.setPhysicalVolumes((List<GlusterLocalPhysicalVolume>) physicalVolumesResult.getReturnValue());
                }
                VDSReturnValue vdoVolumesResult = resourceManager.runVdsCommand(
                        VDSCommandType.GetGlusterVDOVolumeList,
                        new VdsIdVDSCommandParametersBase(vds.getId()));
                if (vdoVolumesResult.getSucceeded()) {
                    localVolumeInfo.setVdoVolumes((List<GlusterVDOVolume>) vdoVolumesResult.getReturnValue());
                }
                localVolumeInfoMap.put(vds.getId(), localVolumeInfo);
            } catch (Exception ex) {
                log.debug("Getting VDSM/VDO information failed at host {}, old vdsm?", vds.getName());
            }
        }
        return localVolumeInfoMap;
    }

    /**
     * Fills BrickProperties with physical size info, if available.
     * @param volumeInfo Cluster's local volume info map.
     * @param brick Brick to operate on
     * @param brickProperties Properties of that brick.
     * @return Same BrickProperties with confirmedFreeSize/totalFreeSize fields updates.
     * In case of missing volume for specified brick or missing device in BrickProperties,
     * original BrickProperties will be returned.
     */
    public BrickProperties setConfirmedSize(Map<Guid, GlusterLocalVolumeInfo> volumeInfo, GlusterBrickEntity brick, BrickProperties brickProperties) {
        if (brickProperties.getDevice() != null && volumeInfo.get(brick.getServerId()) != null) {
            GlusterLocalVolumeInfo localVolumeInfo = volumeInfo.get(brick.getServerId());
            brickProperties.setConfirmedFreeSize(localVolumeInfo.getAvailableThinSizeForDevice(brickProperties.getDevice())
                            .map(this::toMB).orElse(null)
            );

            brickProperties.setConfirmedTotalSize(localVolumeInfo.getTotalThinSizeForDevice(brickProperties.getDevice())
                            .map(this::toMB).orElse(null)
            );
            brickProperties.setVdoSavings(localVolumeInfo.getSavingsForDevice(brickProperties.getDevice()).orElse(null));
        }
        return brickProperties;
    }

    private double toMB(Long bytes) {
        return bytes.doubleValue() / SizeConverter.BYTES_IN_MB;
    }

    private <T> Stream<T> extractBrickData(List<GlusterBrickEntity> bricks, Function<BrickProperties, T> field) {
        return bricks.stream().filter(b -> !b.getIsArbiter()).map(GlusterBrickEntity::getBrickProperties).map(field);
    }

    private <T, R> R calculateConfirmedVolume(GlusterVolumeEntity volume, Function<BrickProperties, T> field, Function<List<Stream<T>>, R> reduce) {
        List<GlusterBrickEntity> bricks = volume.getBricks().stream()
                .map(GlusterBrickEntity::getId)
                .map(b -> brickDao.getById(b))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (extractBrickData(bricks, field).anyMatch(Objects::isNull)) {
            //If we have bricks missing confirmed size, we can't calculate it for the volume.
            log.info("Volume {} have non-thin bricks, skipping confirmed free size calculation", volume.getName());
            return null;
        }

        List<Stream<T>> replicaSets = new ArrayList<>();
        switch (volume.getVolumeType()) {
            case REPLICATE:
            case DISTRIBUTED_REPLICATE:
                while (!bricks.isEmpty()) {
                    int replicaCounter = 0;
                    List<GlusterBrickEntity> set = new ArrayList<>();
                    while(replicaCounter < volume.getReplicaCount() && !bricks.isEmpty()) {
                        set.add(bricks.get(0));
                        bricks.remove(0);
                        replicaCounter++;
                    }
                    replicaSets.add(extractBrickData(set, field));
                }
                break;
            default:
                replicaSets = Collections.singletonList(extractBrickData(bricks, field));
                break;
        }
        return reduce.apply(replicaSets);
    }

    private Function<List<Stream<Double>>, Long> reduceBricksToSize(GlusterVolumeEntity volume) {
        return (List<Stream<Double>> data) -> {
            switch (volume.getVolumeType()) {
                case REPLICATE:
                    return data.stream().flatMap(Function.identity()).map(v -> v * SizeConverter.BYTES_IN_MB).map(Double::longValue).min(Long::compare).orElse(null);
                case DISTRIBUTED_REPLICATE:
                    return data.stream()
                            .map(b -> b.map(v -> v * SizeConverter.BYTES_IN_MB).map(Double::longValue).min(Long::compare).orElse(null))
                            .filter(Objects::nonNull)
                            .mapToLong(s-> s)
                            .sum();
                case DISTRIBUTE:
                case STRIPE:
                case DISTRIBUTED_STRIPE:
                case STRIPED_REPLICATE:
                case DISTRIBUTED_STRIPED_REPLICATE:
                case DISPERSE:
                default:
                    return data.stream().flatMap(Function.identity()).map(v -> v * SizeConverter.BYTES_IN_MB).mapToLong(Double::longValue).sum();
            }
        };
    }

    private Integer reduceBricksToSavings(List<Stream<Integer>> data) {
        return data.stream().flatMap(Function.identity()).collect(Collectors.averagingInt(Integer::intValue)).intValue();
    }

    /**
     * Calculates confirmed free size for a specified volume.
     * @param volume volume to calculate for.
     * @return confirmed free size value in bytes.
     */
    public Long calculateConfirmedVolumeCapacity(GlusterVolumeEntity volume) {
        return calculateConfirmedVolume(volume, BrickProperties::getConfirmedFreeSize, reduceBricksToSize(volume));
    }

    /**
     * Calculates confirmed total size for a specified volume.
     * @param volume volume to calculate for.
     * @return confirmed total size value in bytes.
     */
    public Long calculateConfirmedVolumeTotal(GlusterVolumeEntity volume) {
        return calculateConfirmedVolume(volume, BrickProperties::getConfirmedTotalSize, reduceBricksToSize(volume));
    }

    public Integer calculateVolumeSavings(GlusterVolumeEntity volume) {
        return calculateConfirmedVolume(volume, BrickProperties::getVdoSavings, this::reduceBricksToSavings);
    }

    /**
     * Retrieves list of IDs of storage domains, linked to a specified gluster volume.
     * @param volume volume to operate on
     * @return lis of related storage domain IDs.
     */
    public List<Guid> getVolumeStorageDomains(GlusterVolumeEntity volume) {
        return storageDomainStaticDao.getAllForStoragePool(clusterDao.get(volume.getClusterId()).getStoragePoolId())
                .stream()
                .map(StorageDomainStatic::getId)
                .filter(sd -> storageServerConnectionDao.getAllForDomain(sd)
                        .stream()
                        .anyMatch(c -> volume.getId().equals(c.getGlusterVolumeId())))
                .collect(Collectors.toList());
    }

    /**
     * Updates specified storage domains with new confirmedFreeSize value.
     * @param confirmedFreeSize value to set.
     * @param sdId List of storage domain ID's to update.
     */
    public void setDomainConfirmedFreeSize(Long confirmedFreeSize, Integer vdoSavings, List<Guid> sdId) {
        sdId.stream().map(i -> storageDomainDynamicDao.get(i))
                .forEach(d -> {
                    int confirmedFreeSizeInGb = (int) (confirmedFreeSize / SizeConverter.BYTES_IN_GB);
                    if (confirmedFreeSizeInGb > d.getAvailableDiskSize()) {
                        confirmedFreeSizeInGb = d.getAvailableDiskSize();
                    }
                    d.setConfirmedAvailableDiskSize(confirmedFreeSizeInGb);
                    d.setVdoSavings(vdoSavings);
                    storageDomainDynamicDao.updateConfirmedSize(d);
                });
    }

    /**
     * Sends a notification event in case of excess usage of a thin device.
     * @param confirmedFreeSize currently available space on the storage.
     * @param volume Gluster volume object.
     * @param sdId List of storage domains, that should receive a notification.
     */
    public void sendLowConfirmedSpaceEvent(Long confirmedFreeSize, GlusterVolumeEntity volume, List<Guid> sdId) {
        Long confirmedTotalSize = this.calculateConfirmedVolumeTotal(volume);
        Double percentFreeSize = (confirmedFreeSize.doubleValue() / confirmedTotalSize) * 100;
        Integer freeSpaceThreshold = Config.getValue(ConfigValues.GlusterVolumeFreeSpaceThresholdInPercent);
        if (percentFreeSize <= freeSpaceThreshold.doubleValue()) {
            AuditLogable volumeEvent = new AuditLogableImpl();
            volumeEvent.setGlusterVolumeName(volume.getName());
            volumeEvent.setGlusterVolumeId(volume.getId());
            volumeEvent.setRepeatable(true);
            auditLogDirector.log(volumeEvent, AuditLogType.GLUSTER_VOLUME_CONFIRMED_SPACE_LOW);
        }
        sdId.stream()
                .map(storageDomainStaticDao::get)
                .filter(s -> s.getWarningLowConfirmedSpaceIndicator() != null)
                .filter(s -> s.getWarningLowConfirmedSpaceIndicator() > percentFreeSize)
                .forEach(sd -> {
                    AuditLogable event = new AuditLogableImpl();
                    event.setStorageDomainId(sd.getId());
                    event.setStorageDomainName(sd.getName());
                    event.setRepeatable(true);
                    event.addCustomValue("DiskSpace", String.valueOf(confirmedFreeSize / SizeConverter.BYTES_IN_GB));

                    auditLogDirector.log(event, AuditLogType.IRS_CONFIRMED_DISK_SPACE_LOW);
                });
    }

}
