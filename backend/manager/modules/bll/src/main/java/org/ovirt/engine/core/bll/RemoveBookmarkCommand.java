package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveBookmarkCommand<T extends BookmarksParametersBase> extends BookmarkCommandBase<T> {

    public RemoveBookmarkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        if (getBookmark() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_BOOKMARK_INVALID_ID);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        bookmarkDao.remove(getBookmark().getId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_BOOKMARK : AuditLogType.USER_REMOVE_BOOKMARK_FAILED;
    }
}
