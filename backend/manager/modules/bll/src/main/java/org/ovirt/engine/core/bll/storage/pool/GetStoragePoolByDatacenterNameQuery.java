package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetStoragePoolByDatacenterNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public GetStoragePoolByDatacenterNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(getDbFacade()
                        .getStoragePoolDao()
                        .getByName(getParameters().getName(), true));
    }
}
