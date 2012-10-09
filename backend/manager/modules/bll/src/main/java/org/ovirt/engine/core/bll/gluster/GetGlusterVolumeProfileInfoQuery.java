package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeProfileInfoVDSParameters;
import org.ovirt.engine.core.compat.Guid;


/**
 * Query to fetch gluster volume profile info for the given the volume
 */
public class GetGlusterVolumeProfileInfoQuery<P extends GlusterVolumeQueriesParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeProfileInfoQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDSReturnValue returnValue =
                getBackendResourceManager().RunVdsCommand(VDSCommandType.GetGlusterVolumeProfileInfo,
                        new GlusterVolumeProfileInfoVDSParameters(getParameters().getClusterId(),
                                getUpServerId(getParameters().getClusterId()),
                                getGlusterVolumeName(getParameters().getVolumeId())));
        getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
    }

    private Guid getUpServerId(Guid clusterId) {
        VDS vds = getClusterUtils().getUpServer(clusterId);
        if (vds == null) {
            throw new RuntimeException("No up server found");
        }
        return vds.getId();
    }

    protected ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    protected VDSBrokerFrontend getBackendResourceManager() {
        return getBackend().getResourceManager();
    }
}
