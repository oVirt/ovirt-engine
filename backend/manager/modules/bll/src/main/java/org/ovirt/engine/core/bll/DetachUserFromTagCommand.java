package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachUserFromTagCommand<T extends AttachEntityToTagParameters> extends UserTagMapBase<T> {

    public DetachUserFromTagCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid userGuid : getUserList()) {
                DbUser user = DbFacade.getInstance().getDbUserDao().get(userGuid);
                if (DbFacade.getInstance().getTagDao().getTagUserByTagIdAndByuserId(getTagId(), userGuid) != null) {
                    if (user != null) {
                        appendCustomValue("DetachUsersNames", user.getLoginName(), ", ");
                    }
                    DbFacade.getInstance().getTagDao().detachUserFromTag(getTagId(), userGuid);
                    noActionDone = false;
                    setSucceeded(true);
                }
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return noActionDone ? AuditLogType.UNASSIGNED : getSucceeded() ? AuditLogType.USER_DETACH_USER_FROM_TAG
                : AuditLogType.USER_DETACH_USER_FROM_TAG_FAILED;
    }
}
