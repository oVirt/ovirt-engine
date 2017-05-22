package org.ovirt.engine.core.bll.gluster;

import java.util.Arrays;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;

public class GetGlusterTunedProfilesQuery<P extends IdAndNameQueryParameters> extends QueriesCommandBase<P> {

    public GetGlusterTunedProfilesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String version = getParameters().getName();
        getQueryReturnValue().setReturnValue(Arrays.asList(Config.<String> getValue(ConfigValues.GlusterTunedProfile, version).split(",")));
    }

}
