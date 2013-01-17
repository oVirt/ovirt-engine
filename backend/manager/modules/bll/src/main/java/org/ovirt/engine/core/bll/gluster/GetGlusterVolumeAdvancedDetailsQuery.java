package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeAdvancedDetailsVDSParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Query to get given volume advanced details
 */
public class GetGlusterVolumeAdvancedDetailsQuery<P extends GlusterVolumeAdvancedDetailsParameters> extends GlusterQueriesCommandBase<P> {

    private static final String OPTION_KEY = "nfs.disable";
    private static final String OPTION_VALUE = "off";

    public GetGlusterVolumeAdvancedDetailsQuery(P params) {
        super(params);
    }

    @Override
    protected void executeQueryCommand() {
        String volumeName = getParameters().getVolumeName();
        // To get the service info, the UI will not pass the volume name.
        // In that case engine will fetch the volume name in the database.
        if (!getParameters().isDetailRequired() && StringUtils.isEmpty(volumeName)) {
            volumeName = getVolumeName(getParameters().getClusterId());
        }

        // If there is no volume present in the cluster, then retuen empty Volume Advanced Details
        if (StringUtils.isEmpty(volumeName)) {
            log.error("No volumes found in the cluster.");
            getQueryReturnValue().setReturnValue(new GlusterVolumeAdvancedDetails());
            return;
        }

        VDSReturnValue returnValue =
                getBackendResourceManager().RunVdsCommand(VDSCommandType.GetGlusterVolumeAdvancedDetails,
                        new GlusterVolumeAdvancedDetailsVDSParameters(getUpServerId(getParameters().getClusterId()),
                                getParameters().getClusterId(),
                                volumeName,
                                getParameters().getBrickName(),
                                getParameters().isDetailRequired()));
        getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
    }

    private String getVolumeName(Guid clusterId) {
        String volumeName = "";
        List<GlusterVolumeEntity> nfsEnabledvolumesList =
                getGlusterVolumeDao().getVolumesByOption(clusterId, GlusterStatus.UP, OPTION_KEY, OPTION_VALUE);

        if (nfsEnabledvolumesList.size() > 0) {
            volumeName = nfsEnabledvolumesList.get(0).getName();
        } else {
            // If none of the volume is nfs enabled, then fetch any one of the volume
            List<GlusterVolumeEntity> volumesList =
                    getGlusterVolumeDao().getByClusterId(clusterId);
            for (GlusterVolumeEntity volume : volumesList) {
                if (volume.isOnline()) {
                    volumeName = volume.getName();
                    break;
                }
            }
        }
        return volumeName;
    }

}
