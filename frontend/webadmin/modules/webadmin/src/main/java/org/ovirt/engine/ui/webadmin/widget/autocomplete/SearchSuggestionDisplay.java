package org.ovirt.engine.ui.webadmin.widget.autocomplete;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestBox.SearchSuggestionColumn;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestBox.SuggestCellTable;
import org.ovirt.engine.ui.webadmin.widget.autocomplete.SearchSuggestBox.SuggestionsTableResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

public final class SearchSuggestionDisplay extends DefaultSuggestionDisplay {

    private SuggestBox suggestBox;
    private VerticalPanel searchBoxPanel;
    private PopupPanel suggestionPopup;
    private SuggestCellTable<SearchSuggestion> suggestionsTable;
    private ListDataProvider<SearchSuggestion> suggestionDataProvider;

    public SearchSuggestionDisplay() {
        super();
    }

    private SuggestCellTable<SearchSuggestion> getSuggestionCellList(final Collection<SearchSuggestion> suggestions,
            final SuggestBox suggestBox, final PopupPanel suggestionPopup) {

        this.suggestBox = suggestBox;
        this.suggestionPopup = suggestionPopup;

        // Create suggestions table
        final SuggestCellTable<SearchSuggestion> suggestionsTable =
                new SuggestCellTable<>(suggestions.size(), (Resources) GWT.create(SuggestionsTableResources.class));

        // Create table's column and add it to the table
        SearchSuggestionColumn<SearchSuggestion> suggestColumn = new SearchSuggestionColumn<SearchSuggestion>() {

            @Override
            public SearchSuggestion getValue(SearchSuggestion suggestion) {
                return suggestion;
            }
        };
        suggestionsTable.addColumn(suggestColumn);

        // Create a data provider and bind it to the table
        suggestionDataProvider = new ListDataProvider<>();
        suggestionDataProvider.addDataDisplay(suggestionsTable);

        // Add suggestions to data provider
        List<SearchSuggestion> list = suggestionDataProvider.getList();
        for (SearchSuggestion suggestion : suggestions) {
            list.add(suggestion);
        }

        // Bind a selection model it to the table
        suggestionsTable.setSelectionModel(new SingleSelectionModel<SearchSuggestion>());

        // Set table's properties
        suggestionsTable.setWidth("100%"); //$NON-NLS-1$
        suggestionsTable.setRowCount(suggestions.size());
        suggestionsTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

        // Add enter key press event handler
        suggestionsTable.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    onSelect();
                }

                if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                    hideSuggestions();
                }

            }
        }, KeyDownEvent.getType());

        // Add click event handler
        suggestionsTable.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onSelect();
            }
        }, ClickEvent.getType());

        return suggestionsTable;
    }

    private void onSelect() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                int selectedItemIndex = suggestionsTable.getSelectedItemIndex();
                SearchSuggestion selectedSuggetion = suggestionDataProvider.getList().get(selectedItemIndex);
                if (selectedSuggetion != null) {
                    suggestBox.setText(selectedSuggetion.getReplacementString());
                }
                suggestionPopup.hide();
                suggestBox.setFocus(true);
            }
        });
    }

    @Override
    protected void moveSelectionDown() {
        // Make sure that the menu is actually showing and focus the selected item
        if (suggestionPopup != null && suggestionPopup.isShowing()) {
            suggestionsTable.focusItemByIndex(suggestionsTable.getSelectedItemIndex());

            if (suggestionsTable.getSelectedItemIndex() == 0) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        suggestionsTable.getElement().getParentElement().setScrollTop(0);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void showSuggestions(final SuggestBox suggestBox,
            Collection<? extends Suggestion> suggestions,
            boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
            final SuggestionCallback callback) {

        // Hide popup if not needed
        boolean anySuggestions = suggestions != null && suggestions.size() > 0;
        if (!anySuggestions) {
            hideSuggestions();
            return;
        }

        // Create suggestion popup and suggestions table widget
        if (suggestionPopup == null) {
            suggestionPopup = createPopup();
        }

        // Hide the popup before we manipulate the menu within it.
        if (suggestionPopup.isAttached()) {
            suggestionPopup.hide();
        }

        // Link the popup autoHide to the TextBox.
        // If the suggest box has changed, free the old one first.
        if (suggestBox != null) {
            suggestionPopup.removeAutoHidePartner(suggestBox.getElement());
            suggestionPopup.addAutoHidePartner(suggestBox.getElement());
        }

        // Create suggestions table widget
        suggestionsTable =
                getSuggestionCellList((Collection<SearchSuggestion>) suggestions, suggestBox, suggestionPopup);

        // Add table to popup
        suggestionPopup.setWidget(suggestionsTable);

        // Show the popup under the TextBox.
        suggestionPopup.showRelativeTo(searchBoxPanel);

        int searchBoxWidth = searchBoxPanel.getElement().getClientWidth();
        Element table = (Element) suggestionPopup.getElement().getElementsByTagName("table").getItem(0); //$NON-NLS-1$

        suggestionPopup.getElement().getStyle().setWidth(searchBoxWidth, Unit.PX);
        table.getStyle().setWidth(searchBoxWidth, Unit.PX);
    }

    @Override
    public void hideSuggestions() {
        if (suggestionPopup != null) {
            suggestionPopup.hide(false);
        }
    }

    public void setSearchBoxPanel(VerticalPanel searchBoxPanel) {
        this.searchBoxPanel = searchBoxPanel;
    }
}
