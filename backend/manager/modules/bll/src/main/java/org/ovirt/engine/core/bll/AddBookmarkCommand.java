package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.BookmarkDao;

public class AddBookmarkCommand<T extends BookmarksOperationParameters> extends BookmarkOperationCommand<T> {

    public AddBookmarkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Inject
    private BookmarkDao bookmarkDao;

    @Override
    public void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    @Override
    protected boolean validate() {
        if (bookmarkDao.getByName(getBookmark().getName()) != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        bookmarkDao.save(getBookmark());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_BOOKMARK : AuditLogType.USER_ADD_BOOKMARK_FAILED;
    }
}
