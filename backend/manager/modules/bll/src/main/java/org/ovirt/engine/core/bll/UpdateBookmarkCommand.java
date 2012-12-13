package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateBookmarkCommand<T extends BookmarksOperationParameters>
        extends BookmarkOperationCommand {
    private static final long serialVersionUID = 1L;

    public UpdateBookmarkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = false;
        Bookmark updated = getBookmark();

        if (updated == null) {
            AddInvalidIdErrorMessages(VdcBllMessages.VAR__ACTION__UPDATE);
        } else {
            Bookmark current = DbFacade.getInstance().getBookmarkDao()
                    .getByName(updated.getbookmark_name());

            if (!(current == null || current.getbookmark_id().equals(
                    updated.getbookmark_id()))) {
                AddErrorMessages(
                        VdcBllMessages.VAR__ACTION__UPDATE,
                        VdcBllMessages.ACTION_TYPE_FAILED_BOOKMARK_NAME_ALREADY_EXISTS);
            } else {
                result = true;
            }
        }

        return result;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getBookmarkDao().update(getBookmark());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_BOOKMARK : AuditLogType.USER_UPDATE_BOOKMARK_FAILED;
    }
}
