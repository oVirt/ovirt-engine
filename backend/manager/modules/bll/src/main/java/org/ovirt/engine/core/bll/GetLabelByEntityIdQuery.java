package org.ovirt.engine.core.bll;

import java.util.Collections;
import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.LabelDao;

public class GetLabelByEntityIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    LabelDao labelDao;

    public GetLabelByEntityIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                labelDao.getAllByEntityIds(
                        Collections.singletonList(getParameters().getId())));
    }
}
