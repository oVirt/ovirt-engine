package org.ovirt.engine.core.bll;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetClusterFeaturesByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetClusterFeaturesByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Set<SupportedAdditionalClusterFeature> additionalClusterFeatures =
                getDbFacade().getClusterFeatureDao().getSupportedFeaturesByClusterId(getParameters().getId());
        getQueryReturnValue().setReturnValue(additionalClusterFeatures);
    }
}
