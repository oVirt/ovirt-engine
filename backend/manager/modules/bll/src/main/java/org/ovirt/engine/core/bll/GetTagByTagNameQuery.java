package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetTagByTagNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private TagsDirector tagsDirector;

    public GetTagByTagNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(tagsDirector.getTagByTagName(getParameters().getName()));
    }
}
