package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.queries.gluster.GlusterServiceQueryParameters;

public class GetGlusterClusterServiceByClusterIdQuery<P extends GlusterServiceQueryParameters> extends GlusterQueriesCommandBase<P> {

    public GetGlusterClusterServiceByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<GlusterClusterService> serviceList = null;

        if(getParameters().getServiceType() == null) {
            serviceList = getGlusterClusterServiceDao().getByClusterId(getParameters().getId());
        } else {
            serviceList = new ArrayList<>();
            serviceList.add(getGlusterClusterServiceDao().getByClusterIdAndServiceType(getParameters().getId(),
                        getParameters().getServiceType()));
        }

        getQueryReturnValue().setReturnValue(serviceList);
    }
}
