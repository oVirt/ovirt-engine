package org.ovirt.engine.core.bll;

import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterFeatureDao;

public class GetClusterFeaturesByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ClusterFeatureDao clusterFeatureDao;

    public GetClusterFeaturesByClusterIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Set<SupportedAdditionalClusterFeature> additionalClusterFeatures =
                clusterFeatureDao.getAllByClusterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(additionalClusterFeatures);
    }
}
