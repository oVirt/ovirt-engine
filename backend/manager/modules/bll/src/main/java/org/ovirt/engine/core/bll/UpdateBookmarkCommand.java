package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateBookmarkCommand<T extends BookmarksOperationParameters>
        extends BookmarkOperationCommand<T> {

    public UpdateBookmarkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = false;
        Bookmark updated = getBookmark();

        if (updated == null) {
            addInvalidIdErrorMessages(EngineMessage.VAR__ACTION__UPDATE);
        } else {
            Bookmark current = DbFacade.getInstance().getBookmarkDao()
                    .getByName(updated.getName());

            if (!(current == null || current.getId().equals(
                    updated.getId()))) {
                addErrorMessages(
                        EngineMessage.VAR__ACTION__UPDATE,
                        EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
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
