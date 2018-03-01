package org.ovirt.engine.ui.webadmin.section.main.view.overlay;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.BookmarkPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.widget.bookmark.BookmarkListGroupItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class BookmarkView extends AbstractView implements BookmarkPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Container, BookmarkView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<BookmarkView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    ListGroup bookmarkListGroup;

    @UiField
    @WithElementId
    Button closeButton;

    @UiField
    Column emptyBookmarksColumn;

    @Inject
    public BookmarkView(BookmarkModelProvider modelProvider) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        generateIds();
    }

    @Override
    public void clearBookmarks() {
        bookmarkListGroup.clear();
        emptyBookmarksColumn.setVisible(true);
    }

    @Override
    public HandlerRegistration addBookmark(Bookmark bookmark, BookmarkListModel model, ClickHandler handler) {
        emptyBookmarksColumn.setVisible(false);
        BookmarkListGroupItem item = new BookmarkListGroupItem(bookmark, bookmarkListGroup.getWidgetCount());
        item.addEditClickHandler(event -> {
                model.setSelectedItem(bookmark);
                model.executeCommand(model.getEditCommand());
            }
        );
        item.addRemoveClickHandler(event -> {
                model.setSelectedItem(bookmark);
                model.executeCommand(model.getRemoveCommand());
            }
        );
        bookmarkListGroup.add(item);
        HandlerRegistration handlerRegistration = null;
        if (handler != null) {
            handlerRegistration = item.addAnchorClickHandler(handler);
        }
        return handlerRegistration;
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
