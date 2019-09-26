package org.ovirt.engine.core.bll.gluster;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeAdvancedDetailsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;

/**
 * Query to get volume advanced details
 */
public class GetGlusterVolumeAdvancedDetailsQuery<P extends GlusterVolumeAdvancedDetailsParameters> extends GlusterQueriesCommandBase<P> {

    private static final String OPTION_KEY_NFS_DISABLE = "nfs.disable";
    private static final String OPTION_VALUE_OFF = "off";
    private static final List<GlusterVolumeType> replicateVolumeTypes =
            Arrays.asList(GlusterVolumeType.REPLICATE, GlusterVolumeType.DISTRIBUTED_REPLICATE);
    private GlusterBrickEntity brick = null;
    private Guid clusterId = null;
    private boolean detailRequired = false;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private GlusterDBUtils glusterDBUtils;

    public GetGlusterVolumeAdvancedDetailsQuery(P params, EngineContext engineContext) {
        super(params, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        clusterId = getParameters().getClusterId();
        detailRequired = getParameters().isDetailRequired();
        Guid volumeId = getParameters().getVolumeId();
        if (volumeId != null) {
            GlusterVolumeEntity volume = glusterVolumeDao.getById(volumeId);
            if (volume == null) {
                throw new RuntimeException(EngineMessage.GLUSTER_VOLUME_ID_INVALID.toString());
            }

            brick = getBrick(getParameters().getBrickId());
            getQueryReturnValue().setReturnValue(fetchAdvancedDetails(volume.getName()));
        } else {
            GlusterVolumeAdvancedDetails advancedDetails = getServiceInfo();
            if (advancedDetails != null) {
                List<GlusterServerService> serviceList = advancedDetails.getServiceInfo();
                if (serviceList != null) {
                    for (GlusterServerService service : serviceList) {
                        String hostName = glusterDBUtils.getHostNameOrIP(service.getGlusterHostUuid());
                        if (hostName != null) {
                            service.setHostName(hostName);
                        }
                    }
                }
            }
            getQueryReturnValue().setReturnValue(advancedDetails);
        }
    }

    private GlusterBrickEntity getBrick(Guid brickId) {
        return (brickId == null) ? null : glusterBrickDao.getById(brickId);
    }

    /**
     * To get the service info, the UI will not pass the volume name, in that case engine will fetch the volume name in
     * the database.
     *
     * NFS volume name should be passed to get nfs service details, similarly REPLICATE/DISTRIBUTED_REPLICATE volume
     * name should be passed as an argument to get the SHD details.
     *
     * So to get volume name from database engine will do the following steps.<br>
     * 1. First fetch NFS + REPLICATE/DISTRIBUTED_REPLICATE volume name<br>
     * 2. If not found then fetch the nfs volume name and then fetch REPLICATE/DISTRIBUTED_REPLICATE volume name<br>
     * 3. The VDS query will be called twice, one with nfs volume name and another with replicate volume name, finally
     * combine the service details.
     */
    private GlusterVolumeAdvancedDetails getServiceInfo() {
        // Get Nfs + Replicated/Distributed Replicate volume.
        GlusterVolumeEntity nfsReplicateVolume = getNfsReplicateVolume(clusterId);
        if (nfsReplicateVolume != null) {
            return fetchAdvancedDetails(nfsReplicateVolume.getName());
        }

        // Get Nfs enabled volume
        GlusterVolumeEntity nfsVolume = getNfsVolume(clusterId);
        // Get Replicated volume
        GlusterVolumeEntity replicateVolume = getReplicateVolume(clusterId);

        // If there is no volume present in the cluster, then return empty Volume Advanced Details
        if (nfsVolume == null && replicateVolume == null) {
            log.error("To get service details, no Nfs or Replicated volumes found in the cluster.");
            return new GlusterVolumeAdvancedDetails();
        }

        GlusterVolumeAdvancedDetails nfsServiceInfo = null;
        GlusterVolumeAdvancedDetails shdServiceInfo = null;

        if (nfsVolume != null) {
            nfsServiceInfo = fetchAdvancedDetails(nfsVolume.getName());
        }

        if (replicateVolume != null) {
            shdServiceInfo = fetchAdvancedDetails(replicateVolume.getName());
        }

        if (nfsServiceInfo == null && shdServiceInfo != null) {
            return shdServiceInfo;
        } else if (nfsServiceInfo != null && shdServiceInfo != null) {
            // combine the Nfs + Shd status
            nfsServiceInfo.getServiceInfo().addAll(shdServiceInfo.getServiceInfo());
        }
        return nfsServiceInfo;
    }

    /**
     * Returns the server id on which the brick resides if <br>
     * a) brick id passed is not null <br>
     * b) the brick server is UP <br>
     * Otherwise returns a random up server from the cluster
     */
    protected Guid getUpServerId() {
        if (brick == null) {
            return getRandomUpServerId(clusterId);
        }

        VDS brickServer = vdsDao.get(brick.getServerId());
        if (brickServer.getStatus() == VDSStatus.Up) {
            return brickServer.getId();
        }

        // brick server is down
        return super.getRandomUpServerId(clusterId);
    }

    private GlusterVolumeAdvancedDetails fetchAdvancedDetails(String volumeName) {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeAdvancedDetails,
                        new GlusterVolumeAdvancedDetailsVDSParameters(getUpServerId(),
                                clusterId,
                                volumeName,
                                brick == null ? null : brick.getQualifiedName(),
                                detailRequired));
        GlusterVolumeAdvancedDetails advancedDetails = (GlusterVolumeAdvancedDetails) returnValue.getReturnValue();

        if (brick != null) {
            //We need to update confirmedFreeSize with precalculated value
            advancedDetails.getBrickDetails().forEach(b -> {
                BrickProperties properties = glusterBrickDao.getById(b.getBrickProperties().getBrickId()).getBrickProperties();
                if (properties != null) {
                    double confirmedFreeSize = properties.getConfirmedFreeSize() == null ? properties.getFreeSize() : properties.getConfirmedFreeSize();
                    b.getBrickProperties().setConfirmedFreeSize(confirmedFreeSize);

                    int vdoSavings = properties.getVdoSavings() == null ? 0 : properties.getVdoSavings();
                    b.getBrickProperties().setVdoSavings(vdoSavings);
                } else {
                    b.getBrickProperties().setConfirmedFreeSize(b.getBrickProperties().getFreeSize());
                }
            });
        }
        return  advancedDetails;
    }

    private GlusterVolumeEntity getReplicateVolume(Guid clusterId) {
        List<GlusterVolumeEntity> replicateVolumes = getReplicateVolumes(clusterId, replicateVolumeTypes);
        if (replicateVolumes.size() > 0) {
            return replicateVolumes.get(0);
        }
        return null;
    }

    private GlusterVolumeEntity getNfsVolume(Guid clusterId) {
        List<GlusterVolumeEntity> nfsVolumes = getNfsVolumes(clusterId);
        if (nfsVolumes.size() > 0) {
            return nfsVolumes.get(0);
        }
        return null;
    }

    private GlusterVolumeEntity getNfsReplicateVolume(Guid clusterId) {
        List<GlusterVolumeEntity> nfsReplicateVolumes =
                getReplicateAndNfsVolumes(clusterId, replicateVolumeTypes);
        if (nfsReplicateVolumes.size() > 0) {
            return nfsReplicateVolumes.get(0);
        }
        return null;
    }

    private List<GlusterVolumeEntity> getReplicateAndNfsVolumes(Guid clusterId, List<GlusterVolumeType> volumeTypes) {
        return glusterVolumeDao.getVolumesByStatusTypesAndOption(clusterId,
                GlusterStatus.UP,
                volumeTypes,
                OPTION_KEY_NFS_DISABLE,
                OPTION_VALUE_OFF);
    }

    private List<GlusterVolumeEntity> getNfsVolumes(Guid clusterId) {
        return glusterVolumeDao.getVolumesByOption(clusterId,
                GlusterStatus.UP,
                OPTION_KEY_NFS_DISABLE,
                OPTION_VALUE_OFF);
    }

    private List<GlusterVolumeEntity> getReplicateVolumes(Guid clusterId, List<GlusterVolumeType> volumeTypes) {
        return glusterVolumeDao.getVolumesByStatusAndTypes(clusterId,
                GlusterStatus.UP,
                volumeTypes);
    }

}
