package org.ovirt.engine.core.bll;

import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class GetVdsByNameQuery<P extends IdAndNameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsDao vdsDao;

    public GetVdsByNameQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VDS vds;
        if (getParameters().getId() != null) {
            vds = vdsDao.getByName(getParameters().getName(), getParameters().getId());
        } else {
            Optional<VDS> result = vdsDao.getFirstByName(getParameters().getName());
            vds = result.isPresent() ? result.get() : null;
        }

        getQueryReturnValue().setReturnValue(vds);
    }
}
