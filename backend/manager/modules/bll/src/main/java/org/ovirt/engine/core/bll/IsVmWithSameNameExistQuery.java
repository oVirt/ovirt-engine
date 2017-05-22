package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class IsVmWithSameNameExistQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    public IsVmWithSameNameExistQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(isVmWithSameNameExistStatic(getParameters().getName(),
                getParameters().getDatacenterId()));
    }

    protected boolean isVmWithSameNameExistStatic(String name, Guid datacenterId) {
        return vmDao.getByNameForDataCenter(datacenterId, name, null, false) != null;
    }
}
