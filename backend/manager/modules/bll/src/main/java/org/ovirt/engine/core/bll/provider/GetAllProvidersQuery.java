package org.ovirt.engine.core.bll.provider;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;

public class GetAllProvidersQuery<P extends GetAllProvidersParameters> extends QueriesCommandBase<P> {

    public GetAllProvidersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        ProviderType[] providerTypes = getParameters().getProviderTypes();

        if (providerTypes == null) {
            setReturnValue(getDbFacade().getProviderDao().getAll());
        } else {
            setReturnValue(getDbFacade().getProviderDao().getAllByTypes(providerTypes));
        }
    }
}
