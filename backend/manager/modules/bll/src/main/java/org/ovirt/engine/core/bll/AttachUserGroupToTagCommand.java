package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachUserGroupToTagCommand<T extends AttachEntityToTagParameters> extends UserGroupTagMapBase<T> {

    public AttachUserGroupToTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid groupGuid : getGroupList()) {
                DbGroup group = DbFacade.getInstance().getDbGroupDao().get(groupGuid);
                if (DbFacade.getInstance().getTagDao().getTagUserGroupByGroupIdAndByTagId(getTagId(), groupGuid) == null) {
                    TagsUserGroupMap map = new TagsUserGroupMap(groupGuid, getTagId());
                    DbFacade.getInstance().getTagDao().attachUserGroupToTag(map);
                    noActionDone = false;
                    if (group != null) {
                        appendCustomValue("AttachGroupsNames", group.getName(), ", ");
                    }
                } else {
                    if (group != null) {
                        appendCustomValue("AttachGroupsNamesExists", group.getName(), ", ");
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
