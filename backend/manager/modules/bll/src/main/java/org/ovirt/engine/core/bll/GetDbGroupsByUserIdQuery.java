package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDAO;

public class GetDbGroupsByUserIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetDbGroupsByUserIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final Guid id = getParameters().getId();
        final DbGroupDAO dao = getDbFacade().getDbGroupDao();
        getQueryReturnValue().setReturnValue(dao.getByUserId(id));
    }

}
