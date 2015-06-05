package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeGeoRepConfigListQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeGeoRepConfigListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<GlusterGeoRepSessionConfiguration> configs = getGeoRepDao().getGeoRepSessionConfig(getParameters().getId());
        configs.addAll(getGeoRepDao().getGlusterGeoRepSessionUnSetConfig(getParameters().getId()));
        getQueryReturnValue().setReturnValue(configs);
    }

}
