package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.VmPoolDao;

public class IsVmPoolWithSameNameExistsQuery<P extends NameQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmPoolDao vmPoolDao;

    public IsVmPoolWithSameNameExistsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vmPoolDao.getByName(getParameters().getName()) != null);
    }
}
