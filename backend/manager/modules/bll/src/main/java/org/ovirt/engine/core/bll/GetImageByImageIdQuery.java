package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetImageByImageIdParameters;

public class GetImageByImageIdQuery<P extends GetImageByImageIdParameters>
        extends QueriesCommandBase<P> {
    public GetImageByImageIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getDiskImageDAO().get(getParameters().getImageId()));
    }
}
