package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagByTagIdParameters;

public class GetTagByTagIdQuery<P extends GetTagByTagIdParameters> extends QueriesCommandBase<P> {
    public GetTagByTagIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(TagsDirector.getInstance().GetTagById(getParameters().getTagId()));
    }
}
