package org.ovirt.engine.core.bll.gluster;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeAdvancedDetailsVDSParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Query to get volume advanced details
 */
public class GetGlusterVolumeAdvancedDetailsQuery<P extends GlusterVolumeAdvancedDetailsParameters> extends GlusterQueriesCommandBase<P> {

    private static final String OPTION_KEY_NFS_DISABLE = "nfs.disable";
    private static final String OPTION_VALUE_OFF = "off";
    private static final List<GlusterVolumeType> replicateVolumeTypes =
            Arrays.asList(new GlusterVolumeType[] {
                    GlusterVolumeType.REPLICATE, GlusterVolumeType.DISTRIBUTED_REPLICATE });

    public GetGlusterVolumeAdvancedDetailsQuery(P params) {
        super(params);
    }

    @Override
    protected void executeQueryCommand() {
        String volumeName = getParameters().getVolumeName();
        if (StringUtils.isNotEmpty(volumeName)) {
            getQueryReturnValue().setReturnValue(fetchAdvancedDetails(volumeName));
        } else {
            getQueryReturnValue().setReturnValue(getServiceInfo());
        }
    }

    /*
     * To get the service info, the UI will not pass the volume name, in that case engine will fetch the volume name in
     * the database.
     *
     * NFS volume name should be passed to get nfs service details, similarly REPLICATE/DISTRIBUTED_REPLICATE volume
     * name should be passed as an argument to get the SHD details.
     *
     * So to get volume name from database engine will do the following steps.
     * 1. First fetch NFS + REPLICATE/DISTRIBUTED_REPLICATE volume name
     * 2. If not found then fetch the nfs volume name and then fetch
     * REPLICATE/DISTRIBUTED_REPLICATE volume name
     * 3. The VDS query will be called twice, one with nfs volume name and
     * another with replicate volume name, finally combine the service details.
     */
    private GlusterVolumeAdvancedDetails getServiceInfo() {
        // Get Nfs + Replicated/Distributed Replicate volume.
        GlusterVolumeEntity nfsReplicateVolume = getNfsReplicateVolume(getParameters().getClusterId());
        if (nfsReplicateVolume != null) {
            return fetchAdvancedDetails(nfsReplicateVolume.getName());
        }

        // Get Nfs enabled volume
        GlusterVolumeEntity nfsVolume = getNfsVolume(getParameters().getClusterId());
        // Get Replicated volume
        GlusterVolumeEntity replicateVolume = getReplicateVolume(getParameters().getClusterId());

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

    private GlusterVolumeAdvancedDetails fetchAdvancedDetails(String volumeName) {
        VDSReturnValue returnValue =
                getBackendResourceManager().RunVdsCommand(VDSCommandType.GetGlusterVolumeAdvancedDetails,
                        new GlusterVolumeAdvancedDetailsVDSParameters(getUpServerId(getParameters().getClusterId()),
                                getParameters().getClusterId(),
                                volumeName,
                                getParameters().getBrickName(),
                                getParameters().isDetailRequired()));
        return (GlusterVolumeAdvancedDetails) returnValue.getReturnValue();
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
        return getGlusterVolumeDao().getVolumesByStatusTypesAndOption(clusterId,
                GlusterStatus.UP,
                volumeTypes,
                OPTION_KEY_NFS_DISABLE,
                OPTION_VALUE_OFF);
    }

    private List<GlusterVolumeEntity> getNfsVolumes(Guid clusterId) {
        return getGlusterVolumeDao().getVolumesByOption(clusterId,
                GlusterStatus.UP,
                OPTION_KEY_NFS_DISABLE,
                OPTION_VALUE_OFF);
    }

    private List<GlusterVolumeEntity> getReplicateVolumes(Guid clusterId, List<GlusterVolumeType> volumeTypes) {
        return getGlusterVolumeDao().getVolumesByStatusAndTypes(clusterId,
                GlusterStatus.UP,
                volumeTypes);
    }

}
