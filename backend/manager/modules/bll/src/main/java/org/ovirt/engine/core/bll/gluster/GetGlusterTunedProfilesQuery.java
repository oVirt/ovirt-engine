package org.ovirt.engine.core.bll.gluster;

import java.util.Arrays;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetGlusterTunedProfilesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetGlusterTunedProfilesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(Arrays.asList(Config.<String> getValue(ConfigValues.GlusterTunedProfile).split(",")));
    }

}
