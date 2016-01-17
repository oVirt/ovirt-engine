package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class UpdateBookmarkCommand<T extends BookmarksOperationParameters> extends BookmarkOperationCommand<T> {

    public UpdateBookmarkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean validate() {
        Bookmark updated = getBookmark();
        if (updated == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_BOOKMARK_INVALID_ID);
        }

        Bookmark current = bookmarkDao.getByName(updated.getName());
        if (current != null && !current.getId().equals(updated.getId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        bookmarkDao.update(getBookmark());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_BOOKMARK : AuditLogType.USER_UPDATE_BOOKMARK_FAILED;
    }
}
