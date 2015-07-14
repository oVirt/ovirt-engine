package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class BookmarkCommandBase<T extends BookmarksParametersBase> extends CommandBase<T> {
    private Bookmark mBookmark;
    private String mBookmarkName;

    public BookmarkCommandBase(T parameters) {
        super(parameters);
    }

    public BookmarkCommandBase() {
    }

    protected Bookmark getBookmark() {
        if (mBookmark == null) {
            mBookmark = DbFacade.getInstance().getBookmarkDao()
                    .get(getBookmarkId());
        }
        return mBookmark;
    }

    public String getBookmarkValue() {
        return getBookmark() != null ? getBookmark().getbookmark_value() : null;
    }

    public String getBookmarkName() {
        if (mBookmarkName == null && getBookmark() != null) {
            mBookmarkName = getBookmark().getbookmark_name();
        }
        return mBookmarkName;
    }

    public void setBookmarkName(String value) {
        mBookmarkName = value;
    }

    public Guid getBookmarkId() {
        return getParameters().getBookmarkId();
    }

    protected void addErrorMessages(EngineMessage messageActionTypeParameter, EngineMessage messageReason) {
        addCanDoActionMessage(EngineMessage.VAR__TYPE__BOOKMARK);
        addCanDoActionMessage(messageActionTypeParameter);
        addCanDoActionMessage(messageReason);
    }

    protected void addInvalidIdErrorMessages(EngineMessage messageActionTypeParameter) {
        addErrorMessages(messageActionTypeParameter, EngineMessage.ACTION_TYPE_FAILED_BOOKMARK_INVALID_ID);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.BOOKMARK_MANAGEMENT));
    }

}
