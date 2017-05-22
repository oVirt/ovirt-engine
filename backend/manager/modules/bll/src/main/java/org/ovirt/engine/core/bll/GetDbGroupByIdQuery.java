package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;

public class GetDbGroupByIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DbGroupDao dao;

    public GetDbGroupByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        final Guid id = getParameters().getId();
        final DbGroup group = dao.get(id);
        getQueryReturnValue().setReturnValue(group);
    }
}
