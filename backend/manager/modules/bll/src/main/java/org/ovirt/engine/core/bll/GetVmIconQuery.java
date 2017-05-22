package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmIconDao;

/**
 * Given an icon id it returns icons data in dataurl form
 */
public class GetVmIconQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private VmIconDao vmIconDao;

    public GetVmIconQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmIconDao.get(getParameters().getId()));
    }
}
