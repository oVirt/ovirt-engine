package org.ovirt.engine.ui.webadmin.section.main.view;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.InputGroupAddon;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestBox;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestOracle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Provide search within a list view. The search panel has 3 visible parts:
 * <ul>
 *     <li>Static search prefix (set by the navigation but still a part of the search)</li>
 *     <li>User entered search string
 *     <li>Action buttons (clear, bookmark, search)
 * </ul>
 *
 * @param <M> ListModel the search suggest oracle will work on
 */
public class SearchPanelView<M extends SearchableListModel> extends AbstractView implements SearchPanelPresenterWidget.ViewDef<M> {

    interface ViewUiBinder extends UiBinder<Widget, SearchPanelView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SearchPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    /**
     * Static search string prefix that is correlated to UI navigation
     */
    @UiField
    InputGroupAddon searchStringPrefixLabel;

    /**
     * User entered search string with popup suggestions
     */
    @UiField(provided = true)
    @WithElementId
    final SearchSuggestBox searchStringInput;

    /**
     * Clear the user entered search string
     */
    @UiField
    Button searchBoxClear;

    /**
     * Create a new bookmark for the search string
     */
    @UiField
    @WithElementId("bookmarkButton")
    Button searchBoxBookmark;

    /**
     * Execute the search with the intent to use {@link #getSearchString()} for the search
     */
    @UiField
    @WithElementId("searchButton")
    Button searchBoxSearch;

    @UiField
    @WithElementId("searchBoxBookmarkListDropDownButton")
    Button searchBoxBookmarkListDropDownButton;

    @UiField
    DropDownMenu searchBoxBookmarkListDropDown;

    private final SearchSuggestOracle oracle;

    @Inject
    public SearchPanelView() {
        // Define the oracle that finds suggestions
        oracle = new SearchSuggestOracle();

        // Create suggest box widget
        searchStringInput = new SearchSuggestBox(oracle);
        searchStringInput.ensureDebugId("searchSuggestBox"); //$NON-NLS-1$
        searchStringInput.setAutoSelectEnabled(false);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public String getSearchString() {
        return searchStringPrefixLabel.getText() + searchStringInput.getText();
    }

    @Override
    public void setSearchString(String searchString) {
        if (StringHelper.isNotNullOrEmpty(searchString)
                && searchString.trim().toUpperCase().startsWith(searchStringPrefixLabel.getText().toUpperCase())) {
            searchStringInput.setText(searchString.trim().substring(searchStringPrefixLabel.getText().length()));
        } else {
            searchStringInput.setText(searchString);
        }
    }

    @Override
    public void setSearchStringPrefix(String searchStringPrefix) {
        searchStringPrefixLabel.setText(searchStringPrefix);
        oracle.setSearchPrefix(searchStringPrefix);
    }

    @Override
    public HasClickHandlers getBookmarkButton() {
        return searchBoxBookmark;
    }

    @Override
    public HasClickHandlers getClearButton() {
        return searchBoxClear;
    }

    @Override
    public HasClickHandlers getSearchButton() {
        return searchBoxSearch;
    }

    @Override
    public HasKeyDownHandlers getSearchInputHandlers() {
        return searchStringInput;
    }

    @Override
    public void hideSuggestionBox() {
        searchStringInput.hideSuggestion();
    }

    @Override
    public void setModel(SearchableListModel model) {
        oracle.setModel(model);
    }

    @Override
    public void enableSearchBar(boolean status) {
        searchStringInput.setEnabled(status);
    }

    @Override
    public void clearBookmarks() {
        searchBoxBookmarkListDropDown.clear();
        searchBoxBookmarkListDropDownButton.setEnabled(false);
    }

    @Override
    public HandlerRegistration addAvailableBookmarks(Bookmark bookmark, ClickHandler handler) {
        AnchorListItem bookmarkItem = new AnchorListItem();
        bookmarkItem.setText(bookmark.getName());
        HandlerRegistration registration = bookmarkItem.addClickHandler(handler);
        searchBoxBookmarkListDropDown.add(bookmarkItem);
        searchBoxBookmarkListDropDownButton.setEnabled(searchBoxBookmarkListDropDown.getWidgetCount() > 0);
        return registration;
    }
}
