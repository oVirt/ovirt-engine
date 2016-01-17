package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachUserGroupFromTagCommand<T extends AttachEntityToTagParameters> extends UserGroupTagMapBase<T> {

    public DetachUserGroupFromTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid groupGuid : getGroupList()) {
                DbGroup group = DbFacade.getInstance().getDbGroupDao().get(groupGuid);
                if (DbFacade.getInstance().getTagDao().getTagUserGroupByGroupIdAndByTagId(getTagId(), groupGuid) != null) {
                    if (group != null) {
                        appendCustomValue("DetachGroupsNames", group.getName(), ", ");
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
        return noActionDone ? AuditLogType.UNASSIGNED : getSucceeded() ? AuditLogType.USER_DETACH_USER_GROUP_FROM_TAG
                : AuditLogType.USER_DETACH_USER_GROUP_FROM_TAG_FAILED;
    }
}
