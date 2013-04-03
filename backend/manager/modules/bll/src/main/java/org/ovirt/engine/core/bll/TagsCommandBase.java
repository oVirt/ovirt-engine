package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class TagsCommandBase<T extends TagsActionParametersBase> extends AdminOperationCommandBase<T> {
    private tags mTag = null;
    protected boolean noActionDone = true;

    protected tags getTag() {
        if (mTag == null && getTagId() != null) {
            mTag = DbFacade.getInstance().getTagDao().get(getTagId());
        }
        return mTag;
    }

    protected Guid getTagId() {
        return getParameters().getTagId();
    }

    public String getTagName() {
        return getTag() != null ? getTag().gettag_name() : null;
    }

    public TagsCommandBase(T parameters) {
        super(parameters);

    }
}
