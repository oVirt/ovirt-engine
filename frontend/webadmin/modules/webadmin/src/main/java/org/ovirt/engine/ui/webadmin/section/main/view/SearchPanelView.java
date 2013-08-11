package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestBox;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestOracle;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestionDisplay;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

    protected interface Style extends CssResource {

        String searchBoxLeft();

        String searchBoxLeft_HasSelectedTags();

        String searchBoxRight();

        String searchBoxRight_HasSelectedTags();

        String searchBoxPanel();

        String searchBoxPanel_HasSelectedTags();

        String searchBoxClear();

        String searchBoxClear_HasSelectedTags();

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
    FlowPanel searchBoxClear;

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
    Image clearButton;

    @WithElementId
    @UiField
    Image searchButton;

    @UiField
    HorizontalPanel searchPanelContainer;

    @UiField
    HorizontalPanel searchBoxPanelContainer;

    @UiField
    Style style;

    private final int SEARCH_PANEL_WIDTH = 1000;
    private final SearchSuggestOracle oracle;

    @Inject
    public SearchPanelView(ApplicationConstants constants, final ApplicationResources applicationResources) {
        // Define the oracle that finds suggestions
        oracle = new SearchSuggestOracle();

        // Create suggest box widget
        searchStringInput = new SearchSuggestBox(oracle);
        searchStringInput.ensureDebugId("searchSuggestBox"); //$NON-NLS-1$
        searchStringInput.setAutoSelectEnabled(false);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        addStyles();

        searchStringInput.setSearchBoxPanel(searchBoxPanel);

        bookmarkButton.setResource(applicationResources.bookmarkImage());
        searchButton.setResource(applicationResources.searchButtonImage());
        clearButton.setResource(applicationResources.clearSearchImage());

        clearButton.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                clearButton.setResource(applicationResources.clearSearchImage_mouseOver());

            }

        });

        clearButton.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                clearButton.setResource(applicationResources.clearSearchImage());

            }

        });

        clearButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                clearButton.setResource(applicationResources.clearSearchImage_mouseDown());

            }
        });

        clearButton.addMouseUpHandler(new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {
                clearButton.setResource(applicationResources.clearSearchImage_mouseOver());

            }

        });

        searchPanelContainer.setCellWidth(searchBoxPanel, "1000px"); //$NON-NLS-1$

        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void localize(ApplicationConstants constants) {
        searchLabel.setText(constants.searchLabel());
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
            searchBoxClear.addStyleName(style.searchBoxClear_HasSelectedTags());
        }
        else {
            searchBoxLeft.setStyleName(style.searchBoxLeft());
            searchBoxRight.setStyleName(style.searchBoxRight());
            searchBoxPanel.setStyleName(style.searchBoxPanel());
            searchBoxClear.setStyleName(style.searchBoxClear());
        }
    }

    @Override
    public HasClickHandlers getBookmarkButton() {
        return bookmarkButton;
    }

    @Override
    public HasClickHandlers getClearButton() {
        return clearButton;
    }

    @Override
    public HasClickHandlers getSearchButton() {
        return searchButton;
    }

    @Override
    public HasKeyDownHandlers getSearchInputHandlers() {
        // Workaround GWT bug 3533. SuggestBox mistakenly fires key-downs twice. The workaround is
        // to set the key-down handler on the SuggestBox's internal ValueBox.
        // TODO: fix when https://code.google.com/p/google-web-toolkit/issues/detail?id=3533 is fixed,
        // i.e. no longer return getValueBox() -- just return searchStringInput. current target GWT 2.6.
        return searchStringInput.getValueBox();
    }

    @Override
    public void hideSuggestionBox() {
        ((SearchSuggestionDisplay) searchStringInput.getSuggestionDisplay()).hideSuggestions();
    }

    @Override
    public void setCommonModel(CommonModel commonModel) {
        oracle.setCommonModel(commonModel);
    }

}
