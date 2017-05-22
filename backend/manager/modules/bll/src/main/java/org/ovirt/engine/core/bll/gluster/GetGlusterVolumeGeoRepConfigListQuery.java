package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetGlusterVolumeGeoRepConfigListQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterVolumeGeoRepConfigListQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<GlusterGeoRepSessionConfiguration> configs = glusterGeoRepDao.getGeoRepSessionConfig(getParameters().getId());
        configs.addAll(glusterGeoRepDao.getGlusterGeoRepSessionUnSetConfig(getParameters().getId()));
        getQueryReturnValue().setReturnValue(configs);
    }

}
