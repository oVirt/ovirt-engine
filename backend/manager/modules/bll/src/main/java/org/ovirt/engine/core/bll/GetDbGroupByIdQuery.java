package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;

public class GetDbGroupByIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    public GetDbGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final Guid id = getParameters().getId();
        final DbGroupDao dao = getDbFacade().getDbGroupDao();
        final DbGroup group = dao.get(id);
        getQueryReturnValue().setReturnValue(group);
    }
}
