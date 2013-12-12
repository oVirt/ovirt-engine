package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class AdGroupsHandlingCommandBase<T extends IdParameters> extends CommandBase<T> {
    private LdapGroup mGroup;
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

    public String getAdGroupName() {
        if (mGroupName == null && getAdGroup() != null) {
            mGroupName = getAdGroup().getname();
        }
        return mGroupName;
    }

    protected LdapGroup getAdGroup() {
        if (mGroup == null && !getGroupId().equals(Guid.Empty)) {
            DbGroup dbGroup = DbFacade.getInstance().getDbGroupDao().get(getGroupId());
            mGroup = new LdapGroup(dbGroup);
        }
        return mGroup;
    }

    @Override
    protected String getDescription() {
        return getAdGroupName();
    }

    // TODO to be removed
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getGroupId(), VdcObjectType.User,
                getActionType().getActionGroup()));
    }
}
