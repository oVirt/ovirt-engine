package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class TagsCommandBase<T extends TagsActionParametersBase> extends CommandBase<T> {
    private Tags mTag = null;
    protected boolean noActionDone = true;

    protected Tags getTag() {
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

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.TAG_MANAGEMENT));
    }

}
