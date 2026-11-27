package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Returns i440fx machine types supported by every host in the cluster (intersection,
 * not union). An empty result means no i440fx type is common to all hosts.
 */
public class GetClusterI440fxCommonMachinesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VdsDao vdsDao;

    public GetClusterI440fxCommonMachinesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<String> common = vdsDao.getClusterI440fxCommonMachines(getParameters().getId());
        getQueryReturnValue().setReturnValue(common);
    }
}
