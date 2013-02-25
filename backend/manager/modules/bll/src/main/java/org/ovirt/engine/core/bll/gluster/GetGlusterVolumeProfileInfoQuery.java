package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeProfileInfoVDSParameters;


/**
 * Query to fetch gluster volume profile info for the given the volume
 */
public class GetGlusterVolumeProfileInfoQuery<P extends GlusterVolumeQueriesParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeProfileInfoQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GetGlusterVolumeProfileInfo,
                new GlusterVolumeProfileInfoVDSParameters(getParameters().getClusterId(),
                        getUpServerId(getParameters().getClusterId()),
                        getGlusterVolumeName(getParameters().getVolumeId())));
        getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
    }
}
