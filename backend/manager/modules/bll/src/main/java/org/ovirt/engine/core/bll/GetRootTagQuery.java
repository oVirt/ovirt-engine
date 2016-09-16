package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetRootTagQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    @Inject
    private TagsDirector tagsDirector;

    public GetRootTagQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagsDirector.getRootTag());
    }
}
