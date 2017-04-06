package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.TagDao;

public abstract class TagsCommandBase<T extends TagsActionParametersBase> extends CommandBase<T> {

    @Inject
    private TagDao tagDao;

    private Tags tag = null;
    protected boolean noActionDone = true;

    protected Tags getTag() {
        if (tag == null && getTagId() != null) {
            tag = tagDao.get(getTagId());
        }
        return tag;
    }

    protected Guid getTagId() {
        return getParameters().getTagId();
    }

    public String getTagName() {
        return getTag() != null ? getTag().getTagName() : null;
    }

    public TagsCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.TAG_MANAGEMENT));
    }

}
