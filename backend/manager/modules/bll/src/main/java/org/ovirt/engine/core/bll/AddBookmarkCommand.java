package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddBookmarkCommand<T extends BookmarksOperationParameters> extends BookmarkOperationCommand<T> {
    public AddBookmarkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (DbFacade.getInstance().getBookmarkDao()
                .getByName(getBookmark().getbookmark_name()) != null) {
            addErrorMessages(
                    VdcBllMessages.VAR__ACTION__ADD,
                    VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getBookmarkDao().save(getBookmark());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_BOOKMARK : AuditLogType.USER_ADD_BOOKMARK_FAILED;
    }
}
