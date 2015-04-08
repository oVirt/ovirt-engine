package org.ovirt.engine.core.bll;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.queries.GetClusterFeaturesByVersionAndCategoryParameters;

public class GetClusterFeaturesByVersionAndCategoryQuery<P extends GetClusterFeaturesByVersionAndCategoryParameters> extends QueriesCommandBase<P> {

    public GetClusterFeaturesByVersionAndCategoryQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Set<AdditionalFeature> additionalClusterFeatures =
                getDbFacade().getClusterFeatureDao()
                        .getClusterFeaturesForVersionAndCategory(getParameters().getVersion().getValue(),
                        getParameters().getCategory());
        getQueryReturnValue().setReturnValue(additionalClusterFeatures);
    }
}
