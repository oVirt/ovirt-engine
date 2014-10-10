package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;

public abstract class AdGroupsHandlingCommandBase<T extends IdParameters> extends CommandBase<T> {
    private DbGroup mGroup;
    private String mGroupName;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AdGroupsHandlingCommandBase(Guid commandId) {
        super(commandId);
    }

    public AdGroupsHandlingCommandBase(T parameters) {
        super(parameters);
    }

    protected Guid getGroupId() {
        return getParameters().getId();
    }

    public String getGroupName() {
        if (mGroupName == null && getGroup() != null) {
            mGroupName = getGroup().getName();
        }
        return mGroupName;
    }

    protected DbGroup getGroup() {
        if (mGroup == null && !getGroupId().equals(Guid.Empty)) {
            mGroup = getAdGroupDAO().get(getParameters().getId());
        }
        return mGroup;
    }

    @Override
    protected String getDescription() {
        return getGroupName();
    }

    // TODO to be removed
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getGroupId(), VdcObjectType.User,
                getActionType().getActionGroup()));
    }
}
