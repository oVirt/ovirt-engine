package org.ovirt.engine.ui.webadmin.section.main.view.overlay;

import java.util.Collection;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.BookmarkPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.widget.bookmark.BookmarkListGroupItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class BookmarkView extends AbstractView implements BookmarkPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Container, BookmarkView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    ListGroup bookmarkListGroup;

    @UiField
    Button closeButton;

    @UiField
    Column emptyBookmarksColumn;

    private final BookmarkModelProvider modelProvider;

    @Inject
    public BookmarkView(BookmarkModelProvider modelProvider) {
        this.modelProvider = modelProvider;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @SuppressWarnings("unchecked")
    public void updateBookmarks() {
        BookmarkListModel model = modelProvider.getModel();
        bookmarkListGroup.clear();
        Collection<Bookmark> items = model.getItems();
        if (items != null && !items.isEmpty()) {
            emptyBookmarksColumn.setVisible(false);
            for(Bookmark bookmark: items) {
                BookmarkListGroupItem item = new BookmarkListGroupItem(bookmark);
                item.addEditClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        model.setSelectedItem(bookmark);
                        model.executeCommand(model.getEditCommand());
                    }

                });
                item.addDeleteClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        model.setSelectedItem(bookmark);
                        model.executeCommand(model.getRemoveCommand());
                    }

                });
                bookmarkListGroup.add(item);
            }
        } else {
            emptyBookmarksColumn.setVisible(true);
        }
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

}
