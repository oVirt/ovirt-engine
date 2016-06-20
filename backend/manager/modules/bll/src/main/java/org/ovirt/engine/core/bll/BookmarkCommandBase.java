package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BookmarkDao;

public abstract class BookmarkCommandBase<T extends BookmarksParametersBase> extends CommandBase<T> {

    @Inject
    protected BookmarkDao bookmarkDao;

    private Bookmark bookmark;
    private String bookmarkName;

    public BookmarkCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected Bookmark getBookmark() {
        if (bookmark == null) {
            bookmark = bookmarkDao.get(getBookmarkId());
        }
        return bookmark;
    }

    @Override
    public void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__BOOKMARK);
    }

    /**
     * This method is used by reflection by {@link org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector}
     *
     * @return The bookmark name
     */
    public String getBookmarkName() {
        if (bookmarkName == null && getBookmark() != null) {
            bookmarkName = getBookmark().getName();
        }
        return bookmarkName;
    }

    public Guid getBookmarkId() {
        return getParameters().getBookmarkId();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.BOOKMARK_MANAGEMENT));
    }
}
