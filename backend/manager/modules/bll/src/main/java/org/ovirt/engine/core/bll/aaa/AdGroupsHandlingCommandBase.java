package org.ovirt.engine.core.bll.aaa;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;

public abstract class AdGroupsHandlingCommandBase<T extends IdParameters> extends CommandBase<T> {
    private DbGroup group;
    private String groupName;
    @Inject
    private DbGroupDao dbGroupDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected AdGroupsHandlingCommandBase(Guid commandId) {
        super(commandId);
    }

    public AdGroupsHandlingCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected Guid getGroupId() {
        return getParameters().getId();
    }

    public String getGroupName() {
        if (groupName == null && getGroup() != null) {
            groupName = getGroup().getName();
        }
        return groupName;
    }

    protected DbGroup getGroup() {
        if (group == null && !getGroupId().equals(Guid.Empty)) {
            group = dbGroupDao.get(getParameters().getId());
        }
        return group;
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
