package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.TagsUserGroupMap;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.TagDao;

public class AttachUserGroupToTagCommand<T extends AttachEntityToTagParameters> extends UserGroupTagMapBase<T> {

    @Inject
    private DbGroupDao dbGroupDao;
    @Inject
    private TagDao tagDao;

    public AttachUserGroupToTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid groupGuid : getGroupList()) {
                DbGroup group = dbGroupDao.get(groupGuid);
                if (tagDao.getTagUserGroupByGroupIdAndByTagId(getTagId(), groupGuid) == null) {
                    TagsUserGroupMap map = new TagsUserGroupMap(groupGuid, getTagId());
                    tagDao.attachUserGroupToTag(map);
                    noActionDone = false;
                    if (group != null) {
                        appendCustomCommaSeparatedValue("AttachGroupsNames", group.getName());
                    }
                } else {
                    if (group != null) {
                        appendCustomCommaSeparatedValue("AttachGroupsNamesExists", group.getName());
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
