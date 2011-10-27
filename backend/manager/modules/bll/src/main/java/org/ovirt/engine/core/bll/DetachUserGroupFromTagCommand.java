package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachUserGroupFromTagCommand<T extends AttachEntityToTagParameters> extends UserGroupTagMapBase<T> {

    public DetachUserGroupFromTagCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid groupGuid : getGroupList()) {
                ad_groups group = DbFacade.getInstance().getAdGroupDAO().get(groupGuid);
                if (DbFacade.getInstance().getTagDAO().getTagUserGroupByGroupIdAndByTagId(getTagId(), groupGuid) != null) {
                    if (group != null) {
                        AppendCustomValue("DetachGroupsNames", group.getname(), ", ");
                    }
                    DbFacade.getInstance().getTagDAO().detachUserGroupFromTag(getTagId(), groupGuid);
                    noActionDone = false;
                    setSucceeded(true);
                }
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return noActionDone ? AuditLogType.UNASSIGNED : (getSucceeded() ? AuditLogType.USER_DETACH_USER_GROUP_FROM_TAG
                : AuditLogType.USER_DETACH_USER_GROUP_FROM_TAG_FAILED);
    }
}
