package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmIconDao;

import javax.inject.Inject;

/**
 * Given an icon id it returns icons data in dataurl form
 */
public class GetVmIconQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private VmIconDao vmIconDao;

    public GetVmIconQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmIconDao.get(getParameters().getId()));
    }
}
