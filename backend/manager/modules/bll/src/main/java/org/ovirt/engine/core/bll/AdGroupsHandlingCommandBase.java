package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("AdGroupName") })
public abstract class AdGroupsHandlingCommandBase<T extends AdElementParametersBase> extends CommandBase<T> {
    protected tags _tag;
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
        return getParameters().getAdElementId();
    }

    public String getAdGroupName() {
        if (mGroupName == null && getAdGroup() != null) {
            mGroupName = getAdGroup().getname();
        }
        return mGroupName;
    }

    protected LdapGroup getAdGroup() {
        if (mGroup == null && !getGroupId().equals(Guid.Empty)) {
            mGroup = DbFacade.getInstance().getAdGroupDao().get(getGroupId());
        }
        return mGroup;
    }

    @Override
    protected String getDescription() {
        return getAdGroupName();
    }

    public static LdapGroup initAdGroup(LdapGroup adGroup) {
        LdapGroup dbGroup = DbFacade.getInstance().getAdGroupDao().get(adGroup.getid());
        if (dbGroup == null) {
            DbFacade.getInstance().getAdGroupDao().save(adGroup);
            dbGroup = adGroup;
        }
        return dbGroup;
    }

    // TODO to be removed
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getGroupId(), VdcObjectType.User,
                getActionType().getActionGroup()));
    }
}
