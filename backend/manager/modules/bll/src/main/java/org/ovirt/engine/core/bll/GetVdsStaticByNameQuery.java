package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;

public class GetVdsStaticByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsStaticDao vdsStaticDao;

    public GetVdsStaticByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsStatic vds = vdsStaticDao.getByVdsName(getParameters().getName());

        getQueryReturnValue().setReturnValue(vds);

    }
}
