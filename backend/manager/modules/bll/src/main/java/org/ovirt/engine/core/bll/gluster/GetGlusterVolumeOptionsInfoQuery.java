package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

/**
 * Query to get volume option info
 */
public class GetGlusterVolumeOptionsInfoQuery<P extends GlusterParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeOptionsInfoQuery(P params, EngineContext engineContext) {
        super(params, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(VDSCommandType.GetGlusterVolumeOptionsInfo,
                        new VdsIdVDSCommandParametersBase(getUpServerId(getParameters().getClusterId())));
        getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
    }
}
