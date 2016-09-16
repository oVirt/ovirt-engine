package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetTagByTagIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private TagsDirector tagsDirector;

    public GetTagByTagIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagsDirector.getTagById(getParameters().getId()));
    }
}
