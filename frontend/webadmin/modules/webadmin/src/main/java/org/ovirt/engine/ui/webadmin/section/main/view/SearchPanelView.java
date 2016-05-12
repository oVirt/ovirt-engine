package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestBox;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestOracle;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestionDisplay;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchPanelView extends AbstractView implements SearchPanelPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, SearchPanelView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SearchPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    protected interface Style extends CssResource {

        String searchBoxPanel();

        String searchBoxPanel_HasSelectedTags();

        String searchBoxClear();

        String searchBoxClear_HasSelectedTags();

    }

    @UiField
    Label searchStringPrefixLabel;

    @UiField
    VerticalPanel searchBoxPanel;

    @UiField
    FocusPanel searchBoxClear;

    @UiField
    @WithElementId("bookmarkButton")
    FocusPanel searchBoxBookmark;

    @UiField
    @WithElementId("searchButton")
    FocusPanel searchBoxSearch;

    @UiField(provided = true)
    @WithElementId
    final SearchSuggestBox searchStringInput;

    @UiField
    HorizontalPanel searchPanelContainer;

    @UiField
    HorizontalPanel searchBoxPanelContainer;

    @UiField
    Style style;

    private final int SEARCH_PANEL_WIDTH = 1000;
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
        addStyles();

        searchStringInput.setSearchBoxPanel(searchBoxPanel);

        searchPanelContainer.setCellWidth(searchBoxPanel, "1000px"); //$NON-NLS-1$

        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void addStyles() {
        Element postfixElement = searchBoxPanelContainer.getElement().getElementsByTagName("td").getItem(2); //$NON-NLS-1$
        postfixElement.getStyle().setWidth(100, Unit.PCT);
    }

    @Override
    public String getSearchString() {
        return searchStringInput.getText();
    }

    @Override
    public void setSearchString(String searchString) {
        searchStringInput.setText(searchString);
    }

    @Override
    public void setSearchStringPrefix(String searchStringPrefix) {
        searchStringPrefixLabel.setText(searchStringPrefix);
        oracle.setSearchPrefix(searchStringPrefix);

        // Set search input width
        int searchStringInputWidth = SEARCH_PANEL_WIDTH - searchStringPrefixLabel.getElement().getOffsetWidth();
        searchStringInput.getElement().getStyle().setWidth(searchStringInputWidth, Unit.PX);
    }

    @Override
    public void setHasSearchStringPrefix(boolean hasSearchStringPrefix) {
        searchStringPrefixLabel.setVisible(hasSearchStringPrefix);
    }

    @Override
    public void setHasSelectedTags(boolean hasSelectedTags) {
        if (hasSelectedTags) {
            searchBoxPanel.addStyleName(style.searchBoxPanel_HasSelectedTags());
            searchBoxClear.addStyleName(style.searchBoxClear_HasSelectedTags());
        }
        else {
            searchBoxPanel.setStyleName(style.searchBoxPanel());
            searchBoxClear.setStyleName(style.searchBoxClear());
        }
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
        ((SearchSuggestionDisplay) searchStringInput.getSuggestionDisplay()).hideSuggestions();
    }

    @Override
    public void setCommonModel(CommonModel commonModel) {
        oracle.setCommonModel(commonModel);
    }

    @Override
    public void enableSearchBar(boolean status) {
        searchStringInput.setEnabled(status);
    }
}
