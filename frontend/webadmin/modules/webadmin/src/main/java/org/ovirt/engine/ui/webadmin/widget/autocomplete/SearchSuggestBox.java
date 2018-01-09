package org.ovirt.engine.ui.webadmin.widget.autocomplete;

import java.util.Collection;
import java.util.List;

import org.gwtbootstrap3.client.ui.SuggestBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SuggestItemPartModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * SuggestBox that always uses a bootstrap3 {@link TextBox} for input and a
 * {@link SearchSuggestionDisplay} to show the suggestions.
 */
public class SearchSuggestBox extends SuggestBox implements HasElementId {

    private static final int ZINDEX_SUGGESTION = 2;

    // NOTE: bootstrap3 'SuggestBox.CustomSuggestionDisplay' has some position code
    //       that would be good to borrow but we can't inherit directly since it is
    //       default scoped
    /**
     * Use a CellTable to render search suggestions in a popup below the search input box. It also
     * provides key navigation (escape to close, enter to select, up and down to navigate selections).
     */
    public static class SearchSuggestionDisplay extends DefaultSuggestionDisplay {

        private com.google.gwt.user.client.ui.SuggestBox suggestBox;

        private PopupPanel suggestionPopup;
        private SuggestCellTable<SearchSuggestion> suggestionsTable;
        private ListDataProvider<SearchSuggestion> suggestionDataProvider;

        public SearchSuggestionDisplay() {
            super();
        }

        private SuggestCellTable<SearchSuggestion> getSuggestionCellList(
                final Collection<SearchSuggestion> suggestions,
                final com.google.gwt.user.client.ui.SuggestBox suggestBox,
                final PopupPanel suggestionPopup) {
            this.suggestBox = suggestBox;
            this.suggestionPopup = suggestionPopup;
            suggestionPopup.getElement().getStyle().setZIndex(ZINDEX_SUGGESTION);

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
            suggestionsTable.addDomHandler(event -> {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    onSelect();
                }

                if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                    hideSuggestions();
                }

            }, KeyDownEvent.getType());

            // Add click event handler
            suggestionsTable.addDomHandler(event -> onSelect(), ClickEvent.getType());

            return suggestionsTable;
        }

        private void onSelect() {
            Scheduler.get().scheduleDeferred(() -> {
                int selectedItemIndex = suggestionsTable.getSelectedItemIndex();
                SearchSuggestion selectedSuggetion = suggestionDataProvider.getList().get(selectedItemIndex);
                if (selectedSuggetion != null) {
                    suggestBox.setText(selectedSuggetion.getReplacementString());
                }
                suggestionPopup.hide();
                suggestBox.setFocus(true);
            });
        }

        @Override
        protected void moveSelectionDown() {
            // Make sure that the menu is actually showing and focus the selected item
            if (suggestionPopup != null && suggestionPopup.isShowing()) {
                suggestionsTable.focusItemByIndex(suggestionsTable.getSelectedItemIndex());

                if (suggestionsTable.getSelectedItemIndex() == 0) {
                    Scheduler.get().scheduleDeferred(() -> suggestionsTable.getElement().getParentElement().setScrollTop(0));
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void showSuggestions(
                final com.google.gwt.user.client.ui.SuggestBox suggestBox,
                Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML,
                boolean isAutoSelectEnabled,
                SuggestionCallback callback) {
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

            // Show the popup under the TextBox and make it the same width as the TextBox
            if (suggestBox != null) {
                suggestionPopup.showRelativeTo(suggestBox);

                int suggestBoxInputWidth = suggestBox.getValueBox().getElement().getOffsetWidth();
                suggestionPopup.setWidth(suggestBoxInputWidth + "px"); //$NON-NLS-1$
            }
        }

        @Override
        public void hideSuggestions() {
            if (suggestionPopup != null) {
                suggestionPopup.hide(false);
            }
        }

    }

    // Represent a suggestion cell (use safe html)
    public static class SearchSuggestionCell extends AbstractCell<SearchSuggestion> {

        public SearchSuggestionCell() {
            super();
        }

        @Override
        public void render(Context context, SearchSuggestion suggestion, SafeHtmlBuilder sb) {
            if (suggestion != null) {
                sb.append(getStyledSuggestPart(suggestion.getSuggestionPrefix()));
                sb.append(getStyledSuggestPart(suggestion.getSuggestionPostfix()));
            }
        }

        private SafeHtml getStyledSuggestPart(SuggestItemPartModel suggestionPart) {
            String color = ""; //$NON-NLS-1$

            switch (suggestionPart.getPartType()) {
            case Valid:
                color = "grey"; //$NON-NLS-1$
                break;

            case New:
                color = "black"; //$NON-NLS-1$
                break;

            case Erroneous:
                color = "red"; //$NON-NLS-1$
                break;
            }

            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant("<font color=\"" + color + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(SafeHtmlUtils.fromString(suggestionPart.getPartString()));
            sb.appendHtmlConstant("</font>"); //$NON-NLS-1$

            return sb.toSafeHtml();
        }

    }

    // Represent a table column with SearchSuggestion cells
    public abstract static class SearchSuggestionColumn<T> extends Column<T, SearchSuggestion> {

        public SearchSuggestionColumn() {
            super(new SearchSuggestionCell());
        }

    }

    // Extend CellTable for serving suggestions table
    public static class SuggestCellTable<T> extends CellTable<T> {

        public SuggestCellTable(final int pageSize, Resources resources) {
            super(pageSize, resources);
        }

        public void focusItemByIndex(int index) {
            setKeyboardSelected(index, true, true);
        }

        public int getSelectedItemIndex() {
            return getKeyboardSelectedRow();
        }

        @Override
        protected void onFocus() {
        }

    }

    // SuggestionsTable's custom resources interface
    public interface SuggestionsTableResources extends CellTable.Resources {

        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/SearchSuggestionsCellTable.css" })
        TableStyle cellTableStyle();

    }


    public SearchSuggestBox(SuggestOracle suggestOracle) {
        super(suggestOracle, new TextBox(), new SearchSuggestionDisplay());

        // Show suggestion list box on focus
        getValueBox().addFocusHandler(event -> {
            showSuggestionList();
        });
    }

    public void hideSuggestion() {
        ((SearchSuggestionDisplay)getSuggestionDisplay()).hideSuggestions();
    }

    @Override
    public void setElementId(String elementId) {
        getValueBox().getElement().setId(elementId);
    }
}
