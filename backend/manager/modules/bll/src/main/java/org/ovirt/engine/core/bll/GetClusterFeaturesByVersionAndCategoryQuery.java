package org.ovirt.engine.core.bll;

import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.queries.GetClusterFeaturesByVersionAndCategoryParameters;
import org.ovirt.engine.core.dao.ClusterFeatureDao;

public class GetClusterFeaturesByVersionAndCategoryQuery<P extends GetClusterFeaturesByVersionAndCategoryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ClusterFeatureDao clusterFeatureDao;

    public GetClusterFeaturesByVersionAndCategoryQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Set<AdditionalFeature> additionalClusterFeatures =
                clusterFeatureDao.getClusterFeaturesForVersionAndCategory(getParameters().getVersion().getValue(),
                        getParameters().getCategory());
        getQueryReturnValue().setReturnValue(additionalClusterFeatures);
    }
}
