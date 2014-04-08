package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetVdsStaticByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsStaticByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic vds = getDbFacade().getVdsStaticDao().getByVdsName(getParameters().getName());

        getQueryReturnValue().setReturnValue(vds);

    }
}
