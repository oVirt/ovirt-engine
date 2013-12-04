package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;

public abstract class TagsCommandOperationBase<T extends TagsOperationParameters> extends TagsCommandBase<T> {
    public TagsCommandOperationBase(T parameters) {
        super(parameters);
    }

    @Override
    protected Tags getTag() {
        return getParameters().getTag();
    }
}
