package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllTagsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllTagsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(TagsDirector.getInstance().getAllTags());
    }
}
