package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagUserMapByTagNameParameters;

// NOT IN USE
public class GetTagUserMapByTagNameQuery<P extends GetTagUserMapByTagNameParameters> extends QueriesCommandBase<P> {
    public GetTagUserMapByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(getDbFacade().getTagDAO().getTagUserMapByTagName(getParameters().getTagName()));
    }
}
