package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
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
                LdapGroup group = DbFacade.getInstance().getAdGroupDao().get(groupGuid);
                if (DbFacade.getInstance().getTagDao().getTagUserGroupByGroupIdAndByTagId(getTagId(), groupGuid) != null) {
                    if (group != null) {
                        AppendCustomValue("DetachGroupsNames", group.getname(), ", ");
                    }
                    DbFacade.getInstance().getTagDao().detachUserGroupFromTag(getTagId(), groupGuid);
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
