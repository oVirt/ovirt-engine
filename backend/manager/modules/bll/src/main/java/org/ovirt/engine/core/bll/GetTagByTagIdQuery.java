package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetTagByTagIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetTagByTagIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(TagsDirector.getInstance().getTagById(getParameters().getId()));
    }
}
