package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

/**
 * Query to get volume option info
 */
public class GetGlusterVolumeOptionsInfoQuery<P extends GlusterParameters> extends QueriesCommandBase<P> {

    public GetGlusterVolumeOptionsInfoQuery(P params) {
        super(params);
    }

    @Override
    protected void executeQueryCommand() {
        VDSReturnValue returnValue = getBackendInstance().RunVdsCommand(VDSCommandType.GetGlusterVolumeOptionsInfo,
                new VdsIdVDSCommandParametersBase(getClusterUtils()
                        .getUpServer(getParameters().getClusterId())
                        .getId()));
        getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
    }

    public ClusterUtils getClusterUtils() {
        return ClusterUtils.getInstance();
    }

    public VDSBrokerFrontend getBackendInstance() {
        return Backend.getInstance()
                .getResourceManager();
    }
}
