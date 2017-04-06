package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.TagsUserMap;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.TagDao;

public class AttachUserToTagCommand<T extends AttachEntityToTagParameters> extends UserTagMapBase<T> {

    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private TagDao tagDao;

    public AttachUserToTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        TagsUserMap map;
        if (getTagId() != null) {
            for (Guid userGuid : getUserList()) {
                DbUser user = dbUserDao.get(userGuid);
                if (tagDao.getTagUserByTagIdAndByuserId(getTagId(), userGuid) == null) {
                    map = new TagsUserMap(getTagId(), userGuid);
                    tagDao.attachUserToTag(map);
                    noActionDone = false;
                    if (user != null) {
                        appendCustomCommaSeparatedValue("AttachUsersNames", user.getLoginName());
                    }
                } else {
                    if (user != null) {
                        appendCustomCommaSeparatedValue("AttachUsersNamesExists", user.getLoginName());
                    }
                }
            }
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (noActionDone) {
            return AuditLogType.USER_ATTACH_TAG_TO_USER_EXISTS;
        }
        return getSucceeded() ? AuditLogType.USER_ATTACH_TAG_TO_USER : AuditLogType.USER_ATTACH_TAG_TO_USER_FAILED;
    }
}
