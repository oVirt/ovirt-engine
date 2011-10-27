package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.tags_user_group_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachUserGroupToTagCommand<T extends AttachEntityToTagParameters> extends UserGroupTagMapBase<T> {
    public AttachUserGroupToTagCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid groupGuid : getGroupList()) {
                ad_groups group = DbFacade.getInstance().getAdGroupDAO().get(groupGuid);
                if (DbFacade.getInstance().getTagDAO().getTagUserGroupByGroupIdAndByTagId(getTagId(), groupGuid) == null) {
                    tags_user_group_map map = new tags_user_group_map(groupGuid, getTagId());
                    DbFacade.getInstance().getTagDAO().attachUserGroupToTag(map);
                    noActionDone = false;
                    if (group != null) {
                        AppendCustomValue("AttachGroupsNames", group.getname(), ", ");
                    }
                } else {
                    if (group != null) {
                        AppendCustomValue("AttachGroupsNamesExists", group.getname(), ", ");
                    }
                }
            }
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return noActionDone ? AuditLogType.USER_ATTACH_TAG_TO_USER_GROUP_EXISTS
                : getSucceeded() ? AuditLogType.USER_ATTACH_TAG_TO_USER_GROUP
                        : AuditLogType.USER_ATTACH_TAG_TO_USER_GROUP_FAILED;
    }
}
