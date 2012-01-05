package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.view.AbstractView;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestBox;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestOracle;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestionDisplay;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
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

    @UiField
    InlineLabel searchLabel;

    @UiField
    Label searchStringPrefixLabel;

    @UiField
    VerticalPanel searchBoxPanel;

    @UiField
    FlowPanel searchBoxBookmark;

    @UiField
    HTML searchBoxLeft;

    @UiField
    HTML searchBoxRight;

    @UiField(provided = true)
    @WithElementId
    final SearchSuggestBox searchStringInput;

    @WithElementId
    @UiField
    Image bookmarkButton;

    @WithElementId
    @UiField
    Image searchButton;

    @UiField
    Style style;

    private final int SEARCH_PANEL_WIDTH = 1000;
    private final SearchSuggestOracle oracle;

    @Inject
    public SearchPanelView(ApplicationConstants constants, ApplicationResources applicationResources) {
        // Define the oracle that finds suggestions
        oracle = new SearchSuggestOracle();

        // Create suggest box widget
        searchStringInput = new SearchSuggestBox(oracle);
        searchStringInput.ensureDebugId("searchSuggestBox");
        searchStringInput.setAutoSelectEnabled(false);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);

        searchStringInput.setSearchBoxPanel(searchBoxPanel);

        bookmarkButton.setResource(applicationResources.bookmarkImage());
        searchButton.setResource(applicationResources.searchButtonImage());

        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void localize(ApplicationConstants constants) {
        searchLabel.setText(constants.searchLabel());
    }

    @Override
    public String getSearchString() {
        return searchStringInput.getText();
    }

    @Override
    public String getSearchPrefixString() {
        return searchStringPrefixLabel.getText();
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
            searchBoxLeft.addStyleName(style.searchBoxLeft_HasSelectedTags());
            searchBoxRight.addStyleName(style.searchBoxRight_HasSelectedTags());
            searchBoxPanel.addStyleName(style.searchBoxPanel_HasSelectedTags());
            searchBoxBookmark.addStyleName(style.searchBoxBookmark_HasSelectedTags());
        }
        else {
            searchBoxLeft.setStyleName(style.searchBoxLeft());
            searchBoxRight.setStyleName(style.searchBoxRight());
            searchBoxPanel.setStyleName(style.searchBoxPanel());
            searchBoxBookmark.setStyleName(style.searchBoxBookmark());
        }
    }

    @Override
    public HasClickHandlers getBookmarkButton() {
        return bookmarkButton;
    }

    @Override
    public HasClickHandlers getSearchButton() {
        return searchButton;
    }

    @Override
    public HasKeyDownHandlers getSearchInputHandlers() {
        return searchStringInput;
    }

    @Override
    public void hideSuggestionBox() {
        ((SearchSuggestionDisplay) searchStringInput.getSuggestionDisplay()).hideSuggestions();
    }

    protected interface Style extends CssResource {

        String searchBoxLeft();

        String searchBoxLeft_HasSelectedTags();

        String searchBoxRight();

        String searchBoxRight_HasSelectedTags();

        String searchBoxPanel();

        String searchBoxPanel_HasSelectedTags();

        String searchBoxBookmark();

        String searchBoxBookmark_HasSelectedTags();

    }

}
