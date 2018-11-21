package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class GetAllVmsRunningForMultipleVdsQuery<P extends IdsQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    public GetAllVmsRunningForMultipleVdsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override protected void executeQueryCommand() {
        Map<Guid, List<VM>> hostsToVms = vmDao.getAllRunningForMultipleVds(getParameters().getIds());
        setReturnValue(hostsToVms);
    }
}
