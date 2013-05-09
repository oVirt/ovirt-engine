package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetTagByTagNameParameters;

public class GetTagByTagNameQuery<P extends GetTagByTagNameParameters> extends QueriesCommandBase<P> {
    public GetTagByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(TagsDirector.getInstance().GetTagByName(getParameters().getTagName()));
    }
}
