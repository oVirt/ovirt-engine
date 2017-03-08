package org.ovirt.engine.ui.webadmin.widget.bookmark;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.ovirt.engine.core.common.businessentities.Bookmark;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class BookmarkListGroupItem extends ListGroupItem {
    interface WidgetUiBinder extends UiBinder<Widget, BookmarkListGroupItem> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Button editButton;

    @UiField
    Button deleteButton;

    @UiField
    HTMLPanel name;

    @UiField
    HTMLPanel bookmarkText;

    public BookmarkListGroupItem(Bookmark bookmark) {
        add(WidgetUiBinder.uiBinder.createAndBindUi(this));
        name.getElement().setInnerSafeHtml(SafeHtmlUtils.fromString(bookmark.getName()));
        bookmarkText.getElement().setInnerSafeHtml(SafeHtmlUtils.fromString(bookmark.getValue()));
    }

    public HandlerRegistration addEditClickHandler(ClickHandler handler) {
        return editButton.addClickHandler(handler);
    }

    public HandlerRegistration addDeleteClickHandler(ClickHandler handler) {
        return deleteButton.addClickHandler(handler);
    }
}
