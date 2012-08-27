package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("AdGroupName") })
public abstract class AdGroupsHandlingCommandBase<T extends AdElementParametersBase> extends CommandBase<T> {
    protected tags _tag;
    private ad_groups mGroup;
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

    protected ad_groups getAdGroup() {
        if (mGroup == null && !getGroupId().equals(Guid.Empty)) {
            mGroup = DbFacade.getInstance().getAdGroupDAO().get(getGroupId());
        }
        return mGroup;
    }

    @Override
    protected String getDescription() {
        return getAdGroupName();
    }

    public static ad_groups initAdGroup(ad_groups adGroup) {
        ad_groups dbGroup = DbFacade.getInstance().getAdGroupDAO().get(adGroup.getid());
        if (dbGroup == null) {
            DbFacade.getInstance().getAdGroupDAO().save(adGroup);
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
