package org.ovirt.engine.ui.webadmin.widget.bookmark;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.common.client.ClientUrlUtils;

public class BookmarkListGroupItem extends ListGroupItem {

    private static final String HASH = "#"; //$NON-NLS-1$

    interface WidgetUiBinder extends UiBinder<Widget, BookmarkListGroupItem> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<BookmarkListGroupItem> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @WithElementId
    Button editButton;

    @UiField
    @WithElementId
    Button removeButton;

    @UiField
    HTMLPanel name;

    @UiField
    Anchor bookmarkText;

    public BookmarkListGroupItem(Bookmark bookmark, int index) {
        add(WidgetUiBinder.uiBinder.createAndBindUi(this));
        name.getElement().setInnerSafeHtml(SafeHtmlUtils.fromString(bookmark.getName()));
        bookmarkText.setHref(getHrefFromSearchString(bookmark.getValue()));
        bookmarkText.setText(bookmark.getValue());
        generateIds(index);
    }

    private String getHrefFromSearchString(String searchString) {
        String currentPageUrl = FrontendUrlUtils.getCurrentPageURL();
        String fragment = getFragmentFromSearchString(searchString);
        if (fragment != null) {
            currentPageUrl += HASH + fragment;
            SafeUri result = UriUtils.fromString(currentPageUrl);
            return result.asString();
        }
        return HASH;
    }

    private String getFragmentFromSearchString(String searchString) {
        String[] split = searchString.split(":"); //$NON-NLS-1$
        String result = null;
        if (split.length > 0) {
            String defaultSearchString = split[0];
            result = SearchStringMapping.getPlace(defaultSearchString);
            if (split.length > 1 && result != null) {
                // Search string.
                result += ";search="; //$NON-NLS-1$
                String searchQuery = split[1].trim();
                ClientUrlUtils urlUtils = new ClientUrlUtils();
                searchQuery = urlUtils.encodeQueryString(searchQuery);
                // Need to replace '=' in the search string with \2 so that when it gets passed to the url parser on
                // the new place it can properly replace it. GWTP parses the fragment and blows up if it finds more
                // than one = in the fragment. We are already adding an = with the 'search=' part of the fragment.
                searchQuery = searchQuery.replaceAll("%3D", "\\\\2"); //$NON-NLS-1$ $NON-NLS-2$
                result += searchQuery;
            }
        }
        return result;
    }

    public HandlerRegistration addEditClickHandler(ClickHandler handler) {
        return editButton.addClickHandler(handler);
    }

    public HandlerRegistration addRemoveClickHandler(ClickHandler handler) {
        return removeButton.addClickHandler(handler);
    }

    public HandlerRegistration addAnchorClickHandler(ClickHandler handler) {
        return bookmarkText.addClickHandler(handler);
    }

    protected void generateIds(int index) {
        ViewIdHandler.idHandler.setIdExtension(String.valueOf(index));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
