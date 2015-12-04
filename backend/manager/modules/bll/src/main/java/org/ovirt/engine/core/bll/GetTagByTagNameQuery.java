package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetTagByTagNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public GetTagByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(TagsDirector.getInstance().getTagByName(getParameters().getName()));
    }
}
