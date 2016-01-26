package org.ovirt.engine.ui.webadmin.widget.autocomplete;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SuggestItemPartModel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SearchSuggestBox extends SuggestBox implements HasElementId {

    public SearchSuggestBox(SuggestOracle suggestOracle) {
        super(suggestOracle, new TextBox(), new SearchSuggestionDisplay());

        getTextBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                // Show suggestion list box on focus
                showSuggestionList();
            }
        });

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

    public void setSearchBoxPanel(VerticalPanel searchBoxPanel) {
        ((SearchSuggestionDisplay) this.getSuggestionDisplay()).setSearchBoxPanel(searchBoxPanel);
    }

    @Override
    public void setElementId(String elementId) {
        getTextBox().getElement().setId(elementId);
    }
}
