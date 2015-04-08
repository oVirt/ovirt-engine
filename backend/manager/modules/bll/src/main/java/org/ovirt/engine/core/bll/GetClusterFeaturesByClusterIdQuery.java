package org.ovirt.engine.core.bll;

import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterFeatureDao;

public class GetClusterFeaturesByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ClusterFeatureDao clusterFeatureDao;

    public GetClusterFeaturesByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Set<SupportedAdditionalClusterFeature> additionalClusterFeatures =
                clusterFeatureDao.getSupportedFeaturesByClusterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(additionalClusterFeatures);
    }
}
