package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class BookmarkPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<BookmarkModel, BookmarkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<BookmarkModel> {
    }

    @Inject
    public BookmarkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
