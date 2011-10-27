package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetImageByImageIdQuery<P extends GetImageByImageIdParameters>
        extends QueriesCommandBase<P> {
    public GetImageByImageIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getDiskImageDAO()
                        .get(getParameters().getImageId()));
    }
}
