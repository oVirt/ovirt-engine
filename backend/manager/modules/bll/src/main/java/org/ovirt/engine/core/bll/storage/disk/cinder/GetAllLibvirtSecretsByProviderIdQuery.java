package org.ovirt.engine.core.bll.storage.disk.cinder;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllLibvirtSecretsByProviderIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAllLibvirtSecretsByProviderIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getDbFacade().getLibvirtSecretDao().getAllByProviderId(getParameters().getId()));
    }
}
